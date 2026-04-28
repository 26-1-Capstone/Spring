package com.nutrishare.infrastructure.persistence.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRefreshTokenRepositoryAdapterTest {

    @Mock
    private RefreshTokenRedisRepository redisRepository;

    @InjectMocks
    private RedisRefreshTokenRepositoryAdapter adapter;

    @Test
    void deleteByMemberIdDeletesAllMatchingTokens() {
        List<RefreshTokenEntity> tokens = List.of(
                new RefreshTokenEntity("refresh-1", "123", "access-1"),
                new RefreshTokenEntity("refresh-2", "123", "access-2"));
        when(redisRepository.findAllByMemberId("123")).thenReturn(tokens);

        adapter.deleteByMemberId("123");

        verify(redisRepository).findAllByMemberId("123");
        verify(redisRepository).deleteAll(tokens);
    }
}
