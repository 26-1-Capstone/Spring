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
public class OrderItem {

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName; // Snapshot

    @Column(nullable = false)
    private Long unitPrice; // Snapshot

    @Column(nullable = false)
    private Integer quantity;

    public Long getTotalPrice() {
        return unitPrice * quantity;
    }
}
