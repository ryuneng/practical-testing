package sample.cafekiosk.spring.api.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.api.service.product.request.ProductCreateServiceRequest;
import sample.cafekiosk.spring.api.service.product.response.ProductResponse;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * readOnly = true : 읽기전용
 * CRUD 에서 CUD 동작 X / Only Read
 * JPA : CUD 스냅샷 저장, 변경 감지 X -> 성능 향상
 * 
 * CQRS - Command(CUD) / Read 분리
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductNumberFactory productNumberFactory;

    // 동시성 이슈
    @Transactional
    public ProductResponse createProduct(ProductCreateServiceRequest request) {
//        String nextProductNumber = createNextProductNumber();
        String nextProductNumber = productNumberFactory.createNextProductNumber();

        Product product = request.toEntity(nextProductNumber);
        Product savedProduct = productRepository.save(product);

        return ProductResponse.of(savedProduct);
    }

    public List<ProductResponse> getSellingProducts() {
        List<Product> products = productRepository.findAllBySellingStatusIn(ProductSellingStatus.forDisplay());

        return products.stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    // ++ private 메서드를 테스트하고 싶다면, 객체를 분리할 시점인지 생각해보아야 한다.
    //    ProductNumberFactory 클래스를 생성하여 분리해보도록 하자.
//    private String createNextProductNumber() {
//        String latestProductNumber = productRepository.findLatestProductNumber();
//        if (latestProductNumber == null) {
//            return "001";
//        }
//
//        int latestProductNumberInt = Integer.parseInt(latestProductNumber);
//        int nextProductNumberInt = latestProductNumberInt + 1;
//
//        // 9 -> 009 / 10 -> 010
//        return String.format("%03d", nextProductNumberInt);
//    }
}
