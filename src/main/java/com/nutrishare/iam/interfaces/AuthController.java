package com.nutrishare.iam.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.domain.AccountRepository;
import com.nutrishare.iam.application.AuthService;
import com.nutrishare.iam.application.dto.TokenDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

        private final AuthService authService;
        private final AccountRepository accountRepository;
        private final com.nutrishare.iam.application.TokenProvider tokenProvider;

        @GetMapping("/dev-login")
        public ApiResponse<String> devLogin() {
                Account devAccount = accountRepository.findByEmail("dev@nutrishare.local")
                                .orElseGet(() -> accountRepository.save(Account.create("dev@nutrishare.local", "개발자QA")));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                String.valueOf(devAccount.getId()), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                String token = tokenProvider.createAccessToken(authentication);
                return ApiResponse.success(token);
        }

        @PostMapping("/reissue")
        public ApiResponse<String> reissue(
                        @CookieValue(name = "refresh_token") String refreshToken,
                        HttpServletRequest request,
                        HttpServletResponse response) {
                TokenDto tokenDto = authService.reissue(refreshToken);

                // Update Cookie
                Cookie cookie = new Cookie("refresh_token", tokenDto.refreshToken());
                cookie.setHttpOnly(true);
                cookie.setSecure(request.isSecure());
                cookie.setPath("/");
                cookie.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(cookie);

                return ApiResponse.success(tokenDto.accessToken());
        }
}
