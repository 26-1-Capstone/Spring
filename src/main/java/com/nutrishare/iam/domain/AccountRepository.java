package com.nutrishare.iam.domain;

import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findByEmail(String email);

    Optional<Account> findById(Long id);

    Optional<Account> findByProviderAndProviderUserId(String provider, String providerUserId);

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    void delete(Account account);
}
