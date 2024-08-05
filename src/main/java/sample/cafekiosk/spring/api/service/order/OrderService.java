package sample.cafekiosk.spring.api.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    /**
     * 재고 감소 -> 동시성 문제에 대한 고민이 필요하다.
     * optimistic lock / pessimistic lock / ...
     */
    public OrderResponse createOrder(OrderCreateRequest request, LocalDateTime registeredDateTime) {
        List<String> productNumbers = request.getProductNumbers();
        List<Product> products = findProductsBy(productNumbers);

        deductStockQuantities(products); // 재고 차감

        // ##### 중복 상품 주문 생성 프로세스
        // ### RED 테스트 프로세스
        // 1. 주어진 상품 번호 리스트에 해당하는 모든 상품을 데이터베이스에서 조회한다.
        //  * In절은 중복 데이터를 필터링하기 때문에 productNumbers 리스트에 중복되는 상품번호가 있더라도 결과는 중복이 제거된 products가 조회된다.
        //    따라서, 아래의 products만으로는 중복되는 상품번호 리스트로 주문 생성이 불가하기 때문에 Product 객체를 가공하는 로직을 추가해야 한다.
//        List<Product> products = productRepository.findAllByProductNumberIn(productNumbers);

        // ### GREEN 테스트 프로세스 (2~3 추가)
        // 2. 조회한 상품들을 Map으로 변환한다.
        //    productNumber를 기반으로 Product 객체를 빨리 찾을 수 있게 한다.
//        Map<String, Product> productMap = products.stream()
//                .collect(Collectors.toMap(Product::getProductNumber, p -> p));

        // 3. 원래의 상품 번호 리스트를 순회하면서 productMap에서 해당 상품 번호에 대한 Product 객체를 가져와 리스트로 만든다.
//        List<Product> duplicateProducts = productNumbers.stream()
//                .map(productMap::get)
//                .collect(Collectors.toList());

        // ### REFACTOR 프로세스 1
//        List<Product> products = findProductsBy(productNumbers);

        Order order = Order.create(products, registeredDateTime);
        Order savedOrder = orderRepository.save(order);

        return OrderResponse.of(savedOrder);
    }

    private void deductStockQuantities(List<Product> products) {
        // ##### 재고 차감 프로세스
        // 1. 재고 차감 체크가 필요한 상품들 filter
        List<String> stockProductNumbers = extractStockProductNumbers(products);

        // 2. 재고 엔티티 조회
        Map<String, Stock> stockMap = createStockMapBy(stockProductNumbers);
        // 3. 상품별 counting
        Map<String, Long> productCountingMap = createCountingMapBy(stockProductNumbers);

        // 4. 재고 차감 시도
        for (String stockProductNumber : new HashSet<>(stockProductNumbers)) { // new HashSet<>()으로 상품 중복 제거
            Stock stock = stockMap.get(stockProductNumber);
            int quantity = productCountingMap.get(stockProductNumber).intValue();

            if (stock.isQuantityLessThan(quantity)) {
                throw new IllegalArgumentException("재고가 부족한 상품이 있습니다.");
            }
            stock.deductQuantity(quantity);
        }
    }

    // ### REFACTOR 프로세스 2 (중복 상품 주문 생성 프로세스)
    private List<Product> findProductsBy(List<String> productNumbers) {
        // createOrder의 1번 로직
        List<Product> products = productRepository.findAllByProductNumberIn(productNumbers);
        // createOrder의 2번 로직
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductNumber, p -> p));

        // createOrder의 3번 로직
        return productNumbers.stream()
                .map(productMap::get)
                .collect(Collectors.toList());
    }

    private static List<String> extractStockProductNumbers(List<Product> products) {
        return products.stream()
                .filter(product -> ProductType.containsStockType(product.getType()))
                .map(Product::getProductNumber)
                .collect(Collectors.toList());
    }

    private Map<String, Stock> createStockMapBy(List<String> stockProductNumbers) {
        List<Stock> stocks = stockRepository.findAllByProductNumberIn(stockProductNumbers);
        return stocks.stream()
                .collect(Collectors.toMap(Stock::getProductNumber, s -> s));
    }

    private static Map<String, Long> createCountingMapBy(List<String> stockProductNumbers) {
        return stockProductNumbers.stream()
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));
    }
}
