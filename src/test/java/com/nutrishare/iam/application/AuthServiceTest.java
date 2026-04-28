package com.nutrishare.iam.application;

import com.nutrishare.iam.application.dto.TokenDto;
import com.nutrishare.iam.domain.AccountRepository;
import com.nutrishare.iam.domain.RefreshToken;
import com.nutrishare.iam.domain.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void reissueFailsWhenStoredRefreshTokenMemberAccountIsMissing() {
        Authentication authentication = authentication("123");
        RefreshToken storedToken = new RefreshToken("old-refresh", "123", "old-access");
        when(tokenProvider.validateToken("old-refresh")).thenReturn(true);
        when(refreshTokenRepository.findById("old-refresh")).thenReturn(Optional.of(storedToken));
        when(tokenProvider.getAuthentication("old-refresh")).thenReturn(authentication);
        when(accountRepository.existsById(123L)).thenReturn(false);

        assertThatThrownBy(() -> authService.reissue("old-refresh"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid Refresh Token");

        verify(refreshTokenRepository, never()).delete(storedToken);
        verify(refreshTokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reissueFailsClosedForMalformedStoredMemberId() {
        RefreshToken storedToken = new RefreshToken("old-refresh", "not-a-long", "old-access");
        when(tokenProvider.validateToken("old-refresh")).thenReturn(true);
        when(refreshTokenRepository.findById("old-refresh")).thenReturn(Optional.of(storedToken));
        when(tokenProvider.getAuthentication("old-refresh")).thenReturn(authentication("123"));

        assertThatThrownBy(() -> authService.reissue("old-refresh"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid Refresh Token");

        verify(refreshTokenRepository, never()).delete(storedToken);
        verify(refreshTokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reissueRotatesWhenAccountExists() {
        Authentication authentication = authentication("123");
        RefreshToken storedToken = new RefreshToken("old-refresh", "123", "old-access");
        when(tokenProvider.validateToken("old-refresh")).thenReturn(true);
        when(refreshTokenRepository.findById("old-refresh")).thenReturn(Optional.of(storedToken));
        when(tokenProvider.getAuthentication("old-refresh")).thenReturn(authentication);
        when(accountRepository.existsById(123L)).thenReturn(true);
        when(tokenProvider.createAccessToken(authentication)).thenReturn("new-access");
        when(tokenProvider.createRefreshToken(authentication)).thenReturn("new-refresh");

        TokenDto tokenDto = authService.reissue("old-refresh");

        assertThat(tokenDto.accessToken()).isEqualTo("new-access");
        assertThat(tokenDto.refreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenRepository).delete(storedToken);
        verify(refreshTokenRepository).save(org.mockito.ArgumentMatchers.argThat(token ->
                token.getRefreshToken().equals("new-refresh")
                        && token.getMemberId().equals("123")
                        && token.getAccessToken().equals("new-access")));
    }

    private Authentication authentication(String name) {
        return new UsernamePasswordAuthenticationToken(
                name,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
