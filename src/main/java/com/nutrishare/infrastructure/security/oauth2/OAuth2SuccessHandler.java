package com.nutrishare.infrastructure.security.oauth2;

import com.nutrishare.iam.domain.RefreshToken;
import com.nutrishare.iam.domain.RefreshTokenRepository;
import com.nutrishare.infrastructure.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info("OAuth2 Login Success: {}", oAuth2User.getAttributes());
        log.info("Principal Class: {}", authentication.getPrincipal().getClass().getName());

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // Save Refresh Token to Redis
        // Ideally we should use a consistent ID mapping, but for now using the token
        // itself as ID or subject
        // For strict rotation, we store token -> details
        refreshTokenRepository.save(new RefreshToken(refreshToken, authentication.getName(), accessToken));

        // Add Refresh Token as HttpOnly Cookie
        addRefreshTokenCookie(request, response, refreshToken);

        String redirect = request.getParameter("redirect");
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("redirect", redirect != null && !redirect.isBlank() ? redirect : "/")
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void addRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
    }
}
