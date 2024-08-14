package sample.cafekiosk.spring;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import sample.cafekiosk.spring.client.mail.MailSendClient;

// 환경 통합을 위해 사용될 테스트 추상클래스
@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestSupport {

    // 하위 클래스에서 사용할 수 있도록 protected로 선언
    @MockBean
    protected MailSendClient mailSendClient;
}
