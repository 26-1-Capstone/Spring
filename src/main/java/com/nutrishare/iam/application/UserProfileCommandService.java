package com.nutrishare.iam.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.domain.Address;
import com.nutrishare.iam.infrastructure.persistence.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileCommandService {

    private final AccountJpaRepository accountRepository;

    public void updateProfile(Long userId, String nickname, Address address) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "User not found";
                    }
                });

        account.updateProfile(nickname, address);
    }
}
