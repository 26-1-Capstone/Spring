package com.nutrishare.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

public final class AuthUserExtractor {

    private AuthUserExtractor() {
    }

    public static Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            return Long.valueOf(oAuth2User.getName());
        }

        return Long.valueOf(authentication.getName());
    }
}
