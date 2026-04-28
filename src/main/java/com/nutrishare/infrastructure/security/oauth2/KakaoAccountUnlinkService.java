package com.nutrishare.infrastructure.security.oauth2;

import com.nutrishare.iam.application.SocialAccountUnlinkService;
import com.nutrishare.iam.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoAccountUnlinkService implements SocialAccountUnlinkService {

    private static final String KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    private final String adminKey;
    private final RestTemplate restTemplate;

    @Autowired
    public KakaoAccountUnlinkService(
            @Value("${app.oauth.kakao.admin-key:}") String adminKey,
            RestTemplateBuilder restTemplateBuilder) {
        this(adminKey, restTemplateBuilder.build());
    }

    KakaoAccountUnlinkService(String adminKey, RestTemplate restTemplate) {
        this.adminKey = adminKey;
        this.restTemplate = restTemplate;
    }

    @Override
    public void unlink(Account account) {
        if (!account.isKakaoAccount()) {
            return;
        }
        if (!StringUtils.hasText(account.getProviderUserId())) {
            throw new IllegalStateException("Kakao user id is required for unlink");
        }
        if (!StringUtils.hasText(adminKey)) {
            throw new IllegalStateException("Kakao admin key is required for unlink");
        }

        KakaoUnlinkResponse response = requestUnlink(account.getProviderUserId());
        if (response == null || !account.getProviderUserId().equals(String.valueOf(response.id()))) {
            throw new IllegalStateException("Kakao unlink response does not match account");
        }
    }

    private KakaoUnlinkResponse requestUnlink(String providerUserId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", providerUserId);

        try {
            ResponseEntity<KakaoUnlinkResponse> response = restTemplate.postForEntity(
                    KAKAO_UNLINK_URL,
                    new HttpEntity<>(body, headers),
                    KakaoUnlinkResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            throw new IllegalStateException("Kakao unlink failed", e);
        }
    }

    record KakaoUnlinkResponse(Long id) {
    }
}
