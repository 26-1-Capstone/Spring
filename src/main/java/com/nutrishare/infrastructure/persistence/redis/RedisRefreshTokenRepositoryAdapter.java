package com.nutrishare.infrastructure.persistence.redis;

import com.nutrishare.iam.domain.RefreshToken;
import com.nutrishare.iam.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenRedisRepository redisRepository;

    @Override
    public void save(RefreshToken refreshToken) {
        redisRepository.save(new RefreshTokenEntity(
                refreshToken.getRefreshToken(),
                refreshToken.getMemberId(),
                refreshToken.getAccessToken()));
    }

    @Override
    public Optional<RefreshToken> findById(String refreshToken) {
        return redisRepository.findById(refreshToken)
                .map(entity -> new RefreshToken(
                        entity.getRefreshToken(),
                        entity.getMemberId(),
                        entity.getAccessToken()));
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        redisRepository.deleteById(refreshToken.getRefreshToken());
    }

    @Override
    public void deleteByMemberId(String memberId) {
        redisRepository.deleteAll(redisRepository.findAllByMemberId(memberId));
    }
}
