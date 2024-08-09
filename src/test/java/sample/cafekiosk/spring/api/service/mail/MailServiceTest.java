package sample.cafekiosk.spring.api.service.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sample.cafekiosk.spring.client.mail.MailSendClient;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistory;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// 테스트가 시작될 때 Mockito를 사용해서 Mock 객체를 만들거라는 선언을 해주는 역할
@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    // 1-2. Mock 객체 생성 방법2 (가장 많이 사용)
//    @Mock
//    private MailSendClient mailSendClient;

    @Mock
    private MailSendHistoryRepository mailSendHistoryRepository;

    @Spy
    private MailSendClient mailSendClient;

    // MailService의 생성자를 확인해서 Mock 객체로 선언된 객체들을 Inject해준다. (DI와 같은 일을 수행)
    @InjectMocks
    private MailService mailService;
    
    @DisplayName("메일 전송 테스트")
    @Test
    void sendMail() {
        // given
        // 1-1. Mock 객체 생성 방법1
//        MailSendClient mailSendClient = mock(MailSendClient.class);
//        MailSendHistoryRepository mailSendHistoryRepository = mock(MailSendHistoryRepository.class);

        MailService mailService = new MailService(mailSendClient, mailSendHistoryRepository);

        // 2-1. Stubbing: Mock 객체의 행위를 정의 (mailSendClient.sendEmail()을 호출할 때, 어떤 String 값 4개가 들어오면 true를 return하도록 정의함)
//        when(mailSendClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
//                .thenReturn(true); // 기대하는 반환값 : true

        // 2-2. Spy 문법 (Spy는 실제 객체를 사용하기 때문에, 주로 한 객체에서 일부는 실제 객체의 기능을 쓰고, 나머지 일부만 Stubbing하고 싶을 때 사용)
        // 보통 @Mock을 사용하는 경우가 더 많다.
        doReturn(true)
                .when(mailSendClient)
                .sendEmail(anyString(), anyString(), anyString(), anyString());

        // when
        boolean result = mailService.sendMail("", "", "", "");

        // then
        assertThat(result).isTrue();
        // mailSendHistoryRepository의 save()가 1번 호출됐는지 검증
        verify(mailSendHistoryRepository, times(1)).save(any(MailSendHistory.class));
    }

}