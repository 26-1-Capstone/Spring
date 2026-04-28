package com.nutrishare.iam.infrastructure.persistence;

import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;

    @Override
    public Account save(Account account) {
        log.info("Saving account: {}", account.getEmail());
        Account saved = jpaRepository.save(account);
        log.info("Saved account ID: {}", saved.getId());
        return saved;
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Account> findByProviderAndProviderUserId(String provider, String providerUserId) {
        return jpaRepository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void delete(Account account) {
        jpaRepository.delete(account);
    }
}
