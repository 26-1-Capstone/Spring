package com.nutrishare.infrastructure.security.oauth2;

import com.nutrishare.iam.domain.Account;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoAccountUnlinkServiceTest {

    @Test
    void skipsNonKakaoAccounts() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        KakaoAccountUnlinkService service = new KakaoAccountUnlinkService("admin-key", restTemplate);

        service.unlink(Account.createSocial("google@example.com", "구글", "google", "google-123"));

        server.verify();
    }

    @Test
    void unlinksKakaoAccountWithAdminKeyAndProviderUserId() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        KakaoAccountUnlinkService service = new KakaoAccountUnlinkService("admin-key", restTemplate);

        server.expect(requestTo("https://kapi.kakao.com/v1/user/unlink"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "KakaoAK admin-key"))
                .andExpect(content().string(allOf(
                        containsString("target_id_type=user_id"),
                        containsString("target_id=987654321"))))
                .andRespond(withSuccess("{\"id\":987654321}", MediaType.APPLICATION_JSON));

        service.unlink(Account.createSocial("kakao@example.com", "카카오", "kakao", "987654321"));

        server.verify();
    }

    @Test
    void failsClosedWhenAdminKeyIsMissingForKakaoAccount() {
        KakaoAccountUnlinkService service = new KakaoAccountUnlinkService("", new RestTemplate());

        assertThatThrownBy(() -> service.unlink(Account.createSocial("kakao@example.com", "카카오", "kakao", "987654321")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kakao admin key is required for unlink");
    }

    @Test
    void failsClosedWhenKakaoResponseDoesNotMatchAccount() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        KakaoAccountUnlinkService service = new KakaoAccountUnlinkService("admin-key", restTemplate);

        server.expect(requestTo("https://kapi.kakao.com/v1/user/unlink"))
                .andRespond(withSuccess("{\"id\":111}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.unlink(Account.createSocial("kakao@example.com", "카카오", "kakao", "987654321")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kakao unlink response does not match account");

        server.verify();
    }
}
