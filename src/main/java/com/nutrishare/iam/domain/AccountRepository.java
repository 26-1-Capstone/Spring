package com.nutrishare.iam.domain;

import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findByEmail(String email);

    Optional<Account> findById(Long id);

    boolean existsByEmail(String email);
}
