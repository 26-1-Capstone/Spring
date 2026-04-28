package com.nutrishare.iam.application;

import com.nutrishare.iam.domain.Account;

public interface SocialAccountUnlinkService {
    void unlink(Account account);
}
