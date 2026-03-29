package com.nutrishare.cart.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    private List<CartItem> items = new ArrayList<>();

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Cart(Long userId) {
        this.userId = userId;
    }

    public static Cart create(Long userId) {
        return new Cart(userId);
    }

    public void addItem(Long productId, Integer quantity) {
        Optional<CartItem> existing = items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().addQuantity(quantity);
        } else {
            items.add(new CartItem(productId, quantity));
        }
    }

    public void updateItemQuantity(Long productId, Integer quantity) {
        items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.updateQuantity(quantity));
        // If 0, maybe remove? Spec says "update". Removal allows explicit call.
    }

    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void clear() {
        items.clear();
    }
}
