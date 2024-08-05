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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.*;
import static sample.cafekiosk.spring.domain.product.ProductType.HANDMADE;

@ActiveProfiles("test")
@SpringBootTest
//@DataJpaTest // DataJpaTest는 JPA와 관련된 Bean만 찾기 때문에 OrderService를 찾지 못함
class OrderServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

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
        orderProductRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
    }

    @DisplayName("주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrder() {
        // given
        // 1. 상품을 생성한다.
        LocalDateTime registeredDateTime = LocalDateTime.now();

        Product product1 = createProduct(HANDMADE, "001", 1000);
        Product product2 = createProduct(HANDMADE, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));
        // 2. 주문 생성 시 요청할 OrderCreateRequest 객체에 주문할 상품번호 리스트 필드 값을 설정해둔다.
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "002"))
                .build();

        // when - 주문을 생성한다.
        OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);

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
        // 1. 상품을 생성한다.
        LocalDateTime registeredDateTime = LocalDateTime.now();

        Product product1 = createProduct(HANDMADE, "001", 1000);
        Product product2 = createProduct(HANDMADE, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);
        productRepository.saveAll(List.of(product1, product2, product3));
        // 2. 주문 생성 시 요청할 OrderCreateRequest 객체에 주문할 상품번호 리스트 필드 값을 중복되는 번호로 설정해둔다.
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "001")) // 중복되는 상품번호
                .build();

        // when - 주문을 생성한다.
        OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);

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