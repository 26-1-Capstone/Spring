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
        Long memberId = parseMemberId(storedToken.getMemberId());
        if (!String.valueOf(memberId).equals(authentication.getName()) || !accountRepository.existsById(memberId)) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        // Rotation: Delete old
        refreshTokenRepository.delete(storedToken);

        // Create new
        String newAccessToken = tokenProvider.createAccessToken(authentication);
        String newRefreshToken = tokenProvider.createRefreshToken(authentication);

        // Save new
        RefreshToken newToken = new RefreshToken(newRefreshToken, authentication.getName(), newAccessToken);
        refreshTokenRepository.save(newToken);

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    private Long parseMemberId(String memberId) {
        try {
            return Long.valueOf(memberId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Refresh Token", e);
        }
    }
}
