package sample.cafekiosk.spring.api.service.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.*;
import static sample.cafekiosk.spring.domain.product.ProductType.*;

@ActiveProfiles("test")
//@Transactional
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderService orderService;

    // 1. 전체 테스트를 진행했을 때, createOrderWithDuplicateProductNumbers 테스트가 createOrder 테스트에 영향을 미쳐 createOrder 테스트는 실패하게 된다.
    //   -> Map을 만들면서 키에 대한 (productNumber 001 데이터) 중복이 발생한다.
    // 2. 따라서, @AfterEach문을 통해 데이터 클렌징 작업을 수행해야 한다.
    // 3. ProductRepositoryTest는 데이터 클렌징 작업이 없어도 전체 테스트가 성공하는데, 이유는?
    //   -> @SpringBootTest와 @DataJpaTest 차이에 있다.
    //      DataJpaTest는 @Transactional을 가지고 있어 자동으로 롤백이 되고, SpringBootTest는 @Transactional이 없어서 자동으로 롤백이 되지 않는다.
    @AfterEach
    void tearDown() {
        // ++ OrderProduct가 Product와 Order를 참조하고 있기 때문에
        //    OrderProduct의 순서가 둘보다 아래에 위치한다면, 외래키 제약조건 위반으로 인해 테스트는 실패한다.
        orderProductRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();

        // ++ deleteAllInBatch()는 내부적으로 관계를 맺고 있는 객체(OrderProduct)를 직접 지워야 하지만, deleteAll()은 지우지 않아도 된다.
        //    하지만 deleteAllInBatch()은 테이블 전체를 한번에 지워주는 반면, deleteAll()은 데이터를 건건이 지우기 때문에 실행되는 쿼리가 많아 성능 상 좋지 않다.
//        orderProductRepository.deleteAll();
//        productRepository.deleteAll();
//        orderRepository.deleteAll();
    }

    @DisplayName("주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrder() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.now();

        // 1. 상품을 생성한다.
        Product product1 = createProduct(HANDMADE, "001", 1000);
        Product product2 = createProduct(HANDMADE, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));
        // 2. 주문 생성 시 요청할 OrderCreateRequest 객체에 주문할 상품번호 리스트 필드 값을 설정해둔다.
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "002"))
                .build();

        // when - 주문을 생성한다.
        OrderResponse orderResponse = orderService.createOrder(request.toServiceRequest(), registeredDateTime);

        // then
        // 1. 생성된 주문의 아이디가 NotNull인지 체크한다.
        assertThat(orderResponse.getId()).isNotNull();
        // 2. 주문생성시간과 총주문금액의 값을 체크한다.
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredDateTime, 4000);
        // 3. 주문된 상품 개수를 체크하고, 상품번호와 상품가격의 값을 체크한다.
        assertThat(orderResponse.getProducts()).hasSize(2)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple("001", 1000),
                        tuple("002", 3000)
                );

    }

    @DisplayName("중복되는 상품번호 리스트로 주문을 생성할 수 있다.")
    @Test
    void createOrderWithDuplicateProductNumbers() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.now();

        // 1. 상품을 생성한다.
        Product product1 = createProduct(HANDMADE, "001", 1000);
        Product product2 = createProduct(HANDMADE, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));
        // 2. 주문 생성 시 요청할 OrderCreateRequest 객체에 주문할 상품번호 리스트 필드 값을 중복되는 번호로 설정해둔다.
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "001")) // 중복되는 상품번호
                .build();

        // when - 주문을 생성한다.
        OrderResponse orderResponse = orderService.createOrder(request.toServiceRequest(), registeredDateTime);

        // then
        // 1. 생성된 주문의 아이디가 NotNull인지 체크한다.
        assertThat(orderResponse.getId()).isNotNull();
        // 2. 주문생성시간과 총주문금액의 값을 체크한다.
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredDateTime, 2000);
        // 3. 주문된 상품 개수를 체크하고, 상품번호와 상품가격의 값을 체크한다.
        assertThat(orderResponse.getProducts()).hasSize(2)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple("001", 1000),
                        tuple("001", 1000)
                );
    }

    @DisplayName("재고와 관련된 상품이 포함되어 있는 주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrderWithStock() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.now();

        // 1. 상품을 생성한다.
        Product product1 = createProduct(BOTTLE, "001", 1000);
        Product product2 = createProduct(BAKERY, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));

        // 2. 재고를 생성한다.
        Stock stock1 = Stock.create("001", 2);
        Stock stock2 = Stock.create("002", 2);
        stockRepository.saveAll(List.of(stock1, stock2));

        // 3. 주문 생성 시 요청할 OrderCreateRequest 객체에 주문할 상품번호 리스트 필드 값을 설정해둔다.
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "001", "002", "003"))
                .build();

        // when - 주문을 생성한다.
        OrderResponse orderResponse = orderService.createOrder(request.toServiceRequest(), registeredDateTime);

        // then
        // 1. 생성된 주문의 아이디가 NotNull인지 체크한다.
        assertThat(orderResponse.getId()).isNotNull();
        // 2. 주문생성시간과 총주문금액의 값을 체크한다.
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredDateTime, 10000);
        // 3. 주문된 상품 개수를 체크하고, 상품번호와 상품가격의 값을 체크한다.
        assertThat(orderResponse.getProducts()).hasSize(4)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple("001", 1000),
                        tuple("001", 1000),
                        tuple("002", 3000),
                        tuple("003", 5000)
                );

        // 4. 재고가 잘 감소했는지 확인한다.
        //  - 모든 재고를 조회한 후 재고(재고가 등록되어 있는 상품)의 개수를 체크하고, 각 상품의 재고를 체크한다.
        List<Stock> stocks = stockRepository.findAll();
        assertThat(stocks).hasSize(2)
                .extracting("productNumber", "quantity")
                .containsExactlyInAnyOrder(
                        tuple("001", 0),
                        tuple("002", 1)
                );
    }

    @DisplayName("재고가 부족한 상품으로 주문을 생성하려는 경우 예외가 발생한다.")
    @Test
    void createOrderWithNoStock() {
        // given
        LocalDateTime registeredDateTime = LocalDateTime.now();

        // 1. 상품을 생성한다.
        Product product1 = createProduct(BOTTLE, "001", 1000);
        Product product2 = createProduct(BAKERY, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));

        // 2. 재고를 생성한다.
//        Stock stock1 = Stock.create("001", 1);
        // ++ given 절은 최대한 독립성을 보장해서 구성하는 것이 좋다.
        //   stock1.deductQuantity(1); 부분의 인자값을 3으로 바꾸면 테스트가 실패하게 된다.
        //   stock1을 2개 만든 후, 3개를 감소시키기 때문에 given 절에서 실패하게 되는 것이다.
        //   Stock.create()와 같은 팩토리 메서드는 프로덕션 코드에서 의도를 가지고 만든 메서드이기 때문이다.
        //   따라서, 테스트 코드 작성 시 팩토리 메서드 패턴은 지양하고, 순수한 Builder/생성자를 통해 독립적으로 테스트 환경을 구성하는 것이 좋다.
        Stock stock1 = Stock.create("001", 2);
        Stock stock2 = Stock.create("002", 2);
        stock1.deductQuantity(1); // todo
        stockRepository.saveAll(List.of(stock1, stock2));

        // 3. 주문 생성 시 요청할 OrderCreateRequest 객체에 주문할 상품번호 리스트 필드 값을 설정해둔다.
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "001", "002", "003"))
                .build();

        // when // then
        assertThatThrownBy(() -> orderService.createOrder(request.toServiceRequest(), registeredDateTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족한 상품이 있습니다.");
    }
    
    private Product createProduct(ProductType type, String productNumber, int price) {
        return Product.builder()
                .type(type)
                .productNumber(productNumber)
                .price(price)
                .sellingStatus(SELLING)
                .name("메뉴 이름")
                .build();
    }

}