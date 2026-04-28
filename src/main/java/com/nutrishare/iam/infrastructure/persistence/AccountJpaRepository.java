package com.nutrishare.iam.infrastructure.persistence;

import com.nutrishare.iam.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByProviderAndProviderUserId(String provider, String providerUserId);

    boolean existsByEmail(String email);
}
