package com.nutrishare.iam.application;

import com.nutrishare.iam.application.dto.TokenDto;
import com.nutrishare.iam.domain.AccountRepository;
import com.nutrishare.iam.domain.RefreshToken;
import com.nutrishare.iam.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TokenDto reissue(String oldRefreshToken) {
        if (!tokenProvider.validateToken(oldRefreshToken)) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        RefreshToken storedToken = refreshTokenRepository.findById(oldRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token not found in storage"));

        Authentication authentication = tokenProvider.getAuthentication(oldRefreshToken);
        assertStoredTokenBelongsToActiveAccount(storedToken, authentication);

        invalidateStoredRefreshToken(storedToken);

        String newAccessToken = tokenProvider.createAccessToken(authentication);
        String newRefreshToken = tokenProvider.createRefreshToken(authentication);

        RefreshToken newToken = new RefreshToken(newRefreshToken, authentication.getName(), newAccessToken);
        refreshTokenRepository.save(newToken);
        assertRefreshTokenPersisted(newRefreshToken, authentication.getName());

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    private void assertStoredTokenBelongsToActiveAccount(RefreshToken storedToken, Authentication authentication) {
        Long memberId = parseMemberId(storedToken.getMemberId());
        if (!String.valueOf(memberId).equals(authentication.getName()) || !accountRepository.existsById(memberId)) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }
    }

    private Long parseMemberId(String memberId) {
        try {
            return Long.valueOf(memberId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Refresh Token", e);
        }
    }

    private void invalidateStoredRefreshToken(RefreshToken storedToken) {
        refreshTokenRepository.delete(storedToken);
        if (refreshTokenRepository.findById(storedToken.getRefreshToken()).isPresent()) {
            throw new IllegalStateException("Failed to invalidate refresh token");
        }
    }

    private void assertRefreshTokenPersisted(String refreshToken, String memberId) {
        RefreshToken persistedToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new IllegalStateException("Failed to persist refresh token"));

        if (!persistedToken.getMemberId().equals(memberId)) {
            throw new IllegalStateException("Persisted refresh token subject does not match authentication");
        }
    }
}
