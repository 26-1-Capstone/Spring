package com.nutrishare.iam.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {
    private String zipCode;
    private String addressLine1;
    private String addressLine2;
    private String dong;

    public static Address of(String zipCode, String addressLine1, String addressLine2, String dong) {
        return new Address(zipCode, addressLine1, addressLine2, dong);
    }
}
