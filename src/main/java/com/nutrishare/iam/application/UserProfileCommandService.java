package com.nutrishare.iam.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.domain.AccountRepository;
import com.nutrishare.iam.domain.Address;
import com.nutrishare.iam.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileCommandService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SocialAccountUnlinkService socialAccountUnlinkService;

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

    public void withdraw(Long userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "User not found";
                    }
                });

        socialAccountUnlinkService.unlink(account);
        refreshTokenRepository.deleteByMemberId(String.valueOf(userId));
        accountRepository.delete(account);
    }
}
