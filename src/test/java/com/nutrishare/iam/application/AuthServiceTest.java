package com.nutrishare.iam.application;

import com.nutrishare.iam.application.dto.TokenDto;
import com.nutrishare.iam.domain.RefreshToken;
import com.nutrishare.iam.domain.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String OLD_REFRESH_TOKEN = "old-refresh-token";
    private static final String OLD_ACCESS_TOKEN = "old-access-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";
    private static final String NEW_ACCESS_TOKEN = "new-access-token";
    private static final String MEMBER_ID = "42";

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void reissueInvalidatesOldTokenBeforePersistingAndReturningNewToken() {
        RefreshToken storedToken = new RefreshToken(OLD_REFRESH_TOKEN, MEMBER_ID, OLD_ACCESS_TOKEN);
        RefreshToken persistedNewToken = new RefreshToken(NEW_REFRESH_TOKEN, MEMBER_ID, NEW_ACCESS_TOKEN);
        Authentication authentication = authenticationForMember();

        when(tokenProvider.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenRepository.findById(OLD_REFRESH_TOKEN))
                .thenReturn(Optional.of(storedToken))
                .thenReturn(Optional.empty());
        when(tokenProvider.getAuthentication(OLD_REFRESH_TOKEN)).thenReturn(authentication);
        when(tokenProvider.createAccessToken(authentication)).thenReturn(NEW_ACCESS_TOKEN);
        when(tokenProvider.createRefreshToken(authentication)).thenReturn(NEW_REFRESH_TOKEN);
        when(refreshTokenRepository.findById(NEW_REFRESH_TOKEN)).thenReturn(Optional.of(persistedNewToken));

        TokenDto tokenDto = authService.reissue(OLD_REFRESH_TOKEN);

        assertThat(tokenDto.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
        assertThat(tokenDto.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);

        InOrder inOrder = inOrder(refreshTokenRepository, tokenProvider);
        inOrder.verify(tokenProvider).validateToken(OLD_REFRESH_TOKEN);
        inOrder.verify(refreshTokenRepository).findById(OLD_REFRESH_TOKEN);
        inOrder.verify(tokenProvider).getAuthentication(OLD_REFRESH_TOKEN);
        inOrder.verify(refreshTokenRepository).delete(storedToken);
        inOrder.verify(refreshTokenRepository).findById(OLD_REFRESH_TOKEN);
        inOrder.verify(tokenProvider).createAccessToken(authentication);
        inOrder.verify(tokenProvider).createRefreshToken(authentication);
        inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
        inOrder.verify(refreshTokenRepository).findById(NEW_REFRESH_TOKEN);
    }

    @Test
    void reissueFailsClosedWhenRedisDeleteThrows() {
        RefreshToken storedToken = new RefreshToken(OLD_REFRESH_TOKEN, MEMBER_ID, OLD_ACCESS_TOKEN);
        Authentication authentication = authenticationForMember();

        when(tokenProvider.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenRepository.findById(OLD_REFRESH_TOKEN)).thenReturn(Optional.of(storedToken));
        when(tokenProvider.getAuthentication(OLD_REFRESH_TOKEN)).thenReturn(authentication);
        doThrow(new IllegalStateException("redis unavailable"))
                .when(refreshTokenRepository).delete(storedToken);

        assertThatThrownBy(() -> authService.reissue(OLD_REFRESH_TOKEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("redis unavailable");

        verify(tokenProvider, never()).createAccessToken(any(Authentication.class));
        verify(tokenProvider, never()).createRefreshToken(any(Authentication.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void reissueFailsClosedWhenOldTokenStillExistsAfterDelete() {
        RefreshToken storedToken = new RefreshToken(OLD_REFRESH_TOKEN, MEMBER_ID, OLD_ACCESS_TOKEN);
        Authentication authentication = authenticationForMember();

        when(tokenProvider.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenRepository.findById(OLD_REFRESH_TOKEN))
                .thenReturn(Optional.of(storedToken))
                .thenReturn(Optional.of(storedToken));
        when(tokenProvider.getAuthentication(OLD_REFRESH_TOKEN)).thenReturn(authentication);

        assertThatThrownBy(() -> authService.reissue(OLD_REFRESH_TOKEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to invalidate refresh token");

        verify(tokenProvider, never()).createAccessToken(any(Authentication.class));
        verify(tokenProvider, never()).createRefreshToken(any(Authentication.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void reissueFailsClosedWhenNewTokenCannotBeVerifiedInRedis() {
        RefreshToken storedToken = new RefreshToken(OLD_REFRESH_TOKEN, MEMBER_ID, OLD_ACCESS_TOKEN);
        Authentication authentication = authenticationForMember();

        when(tokenProvider.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenRepository.findById(OLD_REFRESH_TOKEN))
                .thenReturn(Optional.of(storedToken))
                .thenReturn(Optional.empty());
        when(tokenProvider.getAuthentication(OLD_REFRESH_TOKEN)).thenReturn(authentication);
        when(tokenProvider.createAccessToken(authentication)).thenReturn(NEW_ACCESS_TOKEN);
        when(tokenProvider.createRefreshToken(authentication)).thenReturn(NEW_REFRESH_TOKEN);
        when(refreshTokenRepository.findById(NEW_REFRESH_TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.reissue(OLD_REFRESH_TOKEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to persist refresh token");
    }

    @Test
    void reissueRejectsRedisTokenStoredForDifferentSubject() {
        RefreshToken storedToken = new RefreshToken(OLD_REFRESH_TOKEN, "99", OLD_ACCESS_TOKEN);
        Authentication authentication = authenticationForMember();

        when(tokenProvider.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenRepository.findById(OLD_REFRESH_TOKEN)).thenReturn(Optional.of(storedToken));
        when(tokenProvider.getAuthentication(OLD_REFRESH_TOKEN)).thenReturn(authentication);

        assertThatThrownBy(() -> authService.reissue(OLD_REFRESH_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token subject does not match storage");

        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    private Authentication authenticationForMember() {
        return new UsernamePasswordAuthenticationToken(
                MEMBER_ID,
                "",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
