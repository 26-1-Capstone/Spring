package com.nutrishare.ordering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShippingAddress {

    @Column(name = "shipping_zip")
    private String zipCode;

    @Column(name = "shipping_line1")
    private String line1;

    @Column(name = "shipping_line2")
    private String line2;
}
