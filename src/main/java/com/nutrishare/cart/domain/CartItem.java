package com.nutrishare.cart.domain;

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
public class CartItem {

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    public void updateQuantity(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        this.quantity = amount;
    }
}
