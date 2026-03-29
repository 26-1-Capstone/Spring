package com.nutrishare.infrastructure.configuration;

import com.nutrishare.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.nutrishare.infrastructure.security.jwt.JwtTokenProvider;
import com.nutrishare.infrastructure.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtTokenProvider jwtTokenProvider;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final com.nutrishare.infrastructure.security.oauth2.CustomOAuth2UserService customOAuth2UserService;
        private final com.nutrishare.infrastructure.security.oauth2.CustomOidcUserService customOidcUserService;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/v1/auth/**", "/login/**", "/oauth2/**",
                                                                "/favicon.ico", "/", "/swagger-ui/**",
                                                                "/v3/api-docs/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                                                UsernamePasswordAuthenticationFilter.class)
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService)
                                                                .oidcUserService(customOidcUserService))
                                                .successHandler(oAuth2SuccessHandler));

                return http.build();
        }
}
