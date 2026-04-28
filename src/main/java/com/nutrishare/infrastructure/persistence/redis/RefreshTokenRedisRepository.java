package com.nutrishare.infrastructure.persistence.redis;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshTokenEntity, String> {
    List<RefreshTokenEntity> findAllByMemberId(String memberId);
}
