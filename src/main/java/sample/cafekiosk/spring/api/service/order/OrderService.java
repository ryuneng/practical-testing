package sample.cafekiosk.spring.api.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderResponse createOrder(OrderCreateRequest request, LocalDateTime registeredDateTime) {
        List<String> productNumbers = request.getProductNumbers();

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
        List<Product> products = findProductsBy(productNumbers);

        Order order = Order.create(products, registeredDateTime);
        Order savedOrder = orderRepository.save(order);

        return OrderResponse.of(savedOrder);
    }

    // ### REFACTOR 프로세스 2
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
}
