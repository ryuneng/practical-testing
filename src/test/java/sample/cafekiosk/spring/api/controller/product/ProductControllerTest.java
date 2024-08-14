package sample.cafekiosk.spring.api.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sample.cafekiosk.spring.ControllerTestSupport;
import sample.cafekiosk.spring.api.controller.product.dto.request.ProductCreateRequest;
import sample.cafekiosk.spring.api.service.product.ProductService;
import sample.cafekiosk.spring.api.service.product.response.ProductResponse;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Controller 관련 Bean만 올릴 수 있는 가벼운 테스트 어노테이션
//@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest extends ControllerTestSupport {

    // MockMvc: 실제 서버를 기동하지 않고도 Mock(가짜) 객체를 사용해 스프링 MVC 동작을 재현할 수 있는 테스트 프레임워크
//    @Autowired
//    private MockMvc mockMvc;
//
//    // ObjectMapper: 직렬화/역직렬화를 도와주는 Jackson 라이브러리의 주요클래스
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // @MockBean: 스프링 컨테이너에 Mockito로 만든 mock 객체를 주입하는 역할을 하는 어노테이션
//    @MockBean
//    private ProductService productService;

    @DisplayName("신규 상품을 등록한다.")
    @Test
    void createProduct() throws Exception {
        // given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .type(ProductType.HANDMADE)
                .sellingStatus(ProductSellingStatus.SELLING)
                .name("아메리카노")
                .price(4000)
                .build();

        // when // then
        // .perform(): 특정 HTTP 요청을 시뮬레이션하는 메서드
        mockMvc.perform(
                post("/api/v1/products/new") // POST 엔드포인트
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print()) // 자세한 로그 프린트
                .andExpect(status().isOk()); // 성공 응답 기대
    }

    @DisplayName("신규 상품을 등록할 때 상품 타입은 필수값이다.")
    @Test
    void createProductWithoutType() throws Exception {
        // given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .sellingStatus(ProductSellingStatus.SELLING)
                .name("아메리카노")
                .price(4000)
                .build();

        // when // then
        // .perform(): 특정 HTTP 요청을 시뮬레이션하는 메서드
        mockMvc.perform(
                post("/api/v1/products/new") // POST 엔드포인트
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print()) // 자세한 로그 프린트
                .andExpect(status().isBadRequest()) // 400 에러 기대
                // jsonPath로 더 자세한 응답 체크
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("상품 타입은 필수입니다."))
                .andExpect(jsonPath("$.data").isEmpty())
            ;
    }

    @DisplayName("신규 상품을 등록할 때 상품 판매상태는 필수값이다.")
    @Test
    void createProductWithoutSellingStatus() throws Exception {
        // given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .type(ProductType.HANDMADE)
                .name("아메리카노")
                .price(4000)
                .build();

        // when // then
        // .perform(): 특정 HTTP 요청을 시뮬레이션하는 메서드
        mockMvc.perform(
                        post("/api/v1/products/new") // POST 엔드포인트
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print()) // 자세한 로그 프린트
                .andExpect(status().isBadRequest()) // 400 에러 기대
                // jsonPath로 더 자세한 응답 체크
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("상품 판매상태는 필수입니다."))
                .andExpect(jsonPath("$.data").isEmpty())
        ;
    }

    @DisplayName("신규 상품을 등록할 때 상품 이름은 필수값이다.")
    @Test
    void createProductWithoutName() throws Exception {
        // given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .type(ProductType.HANDMADE)
                .sellingStatus(ProductSellingStatus.SELLING)
                .price(4000)
                .build();

        // when // then
        // .perform(): 특정 HTTP 요청을 시뮬레이션하는 메서드
        mockMvc.perform(
                        post("/api/v1/products/new") // POST 엔드포인트
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print()) // 자세한 로그 프린트
                .andExpect(status().isBadRequest()) // 400 에러 기대
                // jsonPath로 더 자세한 응답 체크
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("상품 이름은 필수입니다."))
                .andExpect(jsonPath("$.data").isEmpty())
        ;
    }

    @DisplayName("신규 상품을 등록할 때 상품 가격은 양수이다.")
    @Test
    void createProductWithZeroPrice() throws Exception {
        // given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .type(ProductType.HANDMADE)
                .sellingStatus(ProductSellingStatus.SELLING)
                .name("아메리카노")
                .price(0)
                .build();

        // when // then
        // .perform(): 특정 HTTP 요청을 시뮬레이션하는 메서드
        mockMvc.perform(
                        post("/api/v1/products/new") // POST 엔드포인트
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print()) // 자세한 로그 프린트
                .andExpect(status().isBadRequest()) // 400 에러 기대
                // jsonPath로 더 자세한 응답 체크
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("상품 가격은 양수여야 합니다."))
                .andExpect(jsonPath("$.data").isEmpty())
        ;
    }

    @DisplayName("판매 상품을 조회한다.")
    @Test
    void getSellingProducts() throws Exception {
        // given
        List<ProductResponse> result = List.of();

        when(productService.getSellingProducts()).thenReturn(result);

        // when // then
        // .perform(): 특정 HTTP 요청을 시뮬레이션하는 메서드
        mockMvc.perform(
                get("/api/v1/products/selling") // GET 엔드포인트
            )
            .andDo(print()) // 자세한 로그 프린트
            .andExpect(status().isOk()) // 성공 응답 기대
            // jsonPath로 더 자세한 응답 체크
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data").isArray())
        ;
    }

}