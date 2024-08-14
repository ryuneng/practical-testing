package sample.cafekiosk.spring.api.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sample.cafekiosk.spring.ControllerTestSupport;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.OrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Controller 관련 Bean만 올릴 수 있는 가벼운 테스트 어노테이션
//@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest extends ControllerTestSupport {

//    // MockMvc: 실제 서버를 기동하지 않고도 Mock(가짜) 객체를 사용해 스프링 MVC 동작을 재현할 수 있는 테스트 프레임워크
//    @Autowired
//    private MockMvc mockMvc;
//
//    // ObjectMapper: 직렬화/역직렬화를 도와주는 Jackson 라이브러리의 주요클래스
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // @MockBean: 스프링 컨테이너에 Mockito로 만든 mock 객체를 주입하는 역할을 하는 어노테이션
//    @MockBean
//    private OrderService orderService;

    @DisplayName("신규 주문을 등록한다.")
    @Test
    void createOrder() throws Exception {
        // given
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001"))
                .build();

        // when // then
        // .perform(): 특정 HTTP 요청을 시뮬레이션하는 메서드
        mockMvc.perform(
                        post("/api/v1/orders/new") // POST 엔드포인트
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print()) // 자세한 로그 프린트
                .andExpect(status().isOk()) // 성공 응답 기대
                // jsonPath로 더 자세한 응답 체크
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @DisplayName("신규 주문을 등록할 때 상품번호는 1개 이상이어야 한다.")
    @Test
    void createOrderWithEmptyProductNumbers() throws Exception {
        // given
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of())
                .build();

        // when // then
        // .perform(): 특정 HTTP 요청을 시뮬레이션하는 메서드
        mockMvc.perform(
                        post("/api/v1/orders/new") // POST 엔드포인트
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print()) // 자세한 로그 프린트
                .andExpect(status().isBadRequest()) // 400 에러 기대
                // jsonPath로 더 자세한 응답 체크
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("상품 번호 리스트는 필수입니다."))
                .andExpect(jsonPath("$.data").isEmpty())
        ;
    }

}