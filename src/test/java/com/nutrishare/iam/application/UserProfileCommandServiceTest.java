package com.nutrishare.iam.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.domain.AccountRepository;
import com.nutrishare.iam.domain.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileCommandServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserProfileCommandService userProfileCommandService;

    @Test
    void withdrawDeletesAccountAndRefreshTokens() {
        Account account = Account.create("user@example.com", "사용자");
        when(accountRepository.findById(123L)).thenReturn(Optional.of(account));

        userProfileCommandService.withdraw(123L);

        verify(refreshTokenRepository).deleteByMemberId("123");
        verify(accountRepository).delete(account);
    }

    @Test
    void withdrawThrowsNotFoundForMissingAccount() {
        when(accountRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileCommandService.withdraw(123L))
                .isInstanceOf(DomainException.class);

        verify(refreshTokenRepository, never()).deleteByMemberId("123");
    }
}
