package com.nutrishare.infrastructure.persistence.redis;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshTokenEntity, String> {
}
