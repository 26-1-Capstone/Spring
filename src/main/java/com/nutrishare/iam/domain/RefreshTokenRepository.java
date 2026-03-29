package com.nutrishare.iam.domain;

import java.util.Optional;

public interface RefreshTokenRepository {
    void save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(String refreshToken);

    void delete(RefreshToken refreshToken);
}
