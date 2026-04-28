package com.nutrishare.iam.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.domain.AccountRepository;
import com.nutrishare.iam.domain.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileCommandServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SocialAccountUnlinkService socialAccountUnlinkService;

    @InjectMocks
    private UserProfileCommandService userProfileCommandService;

    @Test
    void withdrawUnlinksSocialAccountThenDeletesRefreshTokensAndAccount() {
        Account account = Account.createSocial("user@example.com", "사용자", "kakao", "987654321");
        when(accountRepository.findById(123L)).thenReturn(Optional.of(account));

        userProfileCommandService.withdraw(123L);

        InOrder inOrder = inOrder(socialAccountUnlinkService, refreshTokenRepository, accountRepository);
        inOrder.verify(socialAccountUnlinkService).unlink(account);
        inOrder.verify(refreshTokenRepository).deleteByMemberId("123");
        inOrder.verify(accountRepository).delete(account);
    }

    @Test
    void withdrawStopsBeforeLocalDeletionWhenSocialUnlinkFails() {
        Account account = Account.createSocial("user@example.com", "사용자", "kakao", "987654321");
        when(accountRepository.findById(123L)).thenReturn(Optional.of(account));
        org.mockito.Mockito.doThrow(new IllegalStateException("Kakao unlink failed"))
                .when(socialAccountUnlinkService).unlink(account);

        assertThatThrownBy(() -> userProfileCommandService.withdraw(123L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kakao unlink failed");

        verify(refreshTokenRepository, never()).deleteByMemberId("123");
        verify(accountRepository, never()).delete(account);
    }

    @Test
    void withdrawThrowsNotFoundForMissingAccount() {
        when(accountRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileCommandService.withdraw(123L))
                .isInstanceOf(DomainException.class);

        verify(socialAccountUnlinkService, never()).unlink(org.mockito.ArgumentMatchers.any());
        verify(refreshTokenRepository, never()).deleteByMemberId("123");
    }
}
