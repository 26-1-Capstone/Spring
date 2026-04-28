package com.nutrishare.infrastructure.security.oauth2;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AttributeTest {

    @Test
    void kakaoAttributeKeepsTopLevelProviderUserIdForUnlink() {
        OAuth2Attribute attribute = OAuth2Attribute.of("kakao", "id", Map.of(
                "id", 987654321L,
                "kakao_account", Map.of(
                        "email", "kakao@example.com",
                        "profile", Map.of(
                                "nickname", "카카오사용자",
                                "profile_image_url", "https://example.com/profile.png"))));

        assertThat(attribute.getProvider()).isEqualTo("kakao");
        assertThat(attribute.getProviderUserId()).isEqualTo("987654321");
        assertThat(attribute.getEmail()).isEqualTo("kakao@example.com");
        assertThat(attribute.convertToMap()).containsEntry("id", "987654321");
    }
}
