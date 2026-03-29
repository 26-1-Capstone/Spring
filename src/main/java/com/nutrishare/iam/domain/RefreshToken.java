package com.nutrishare.iam.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshToken {
    private final String refreshToken;
    private final String memberId;
    private final String accessToken;

    // Domain methods/logic could be added here if needed
}
