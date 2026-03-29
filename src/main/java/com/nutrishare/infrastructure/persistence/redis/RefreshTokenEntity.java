package com.nutrishare.infrastructure.persistence.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 604800) // 7 days
public class RefreshTokenEntity {

    @Id
    private String refreshToken;

    private String memberId;

    private String accessToken;

    // Default constructor for Redis
    public RefreshTokenEntity() {
        this.refreshToken = null;
        this.memberId = null;
        this.accessToken = null;
    }
}
