package com.nutrishare.cart.application;

import com.nutrishare.cart.domain.Cart;
import com.nutrishare.cart.domain.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartCommandService {

    private final CartRepository cartRepository;

    public void addItemToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.create(userId)));

        cart.addItem(productId, quantity);
        // Dirty checking saves changes
    }

    public void updateItemQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        if (quantity <= 0) {
            cart.removeItem(productId);
        } else {
            cart.updateItemQuantity(productId, quantity);
        }
    }

    public void removeItemFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        cart.removeItem(productId);
    }
}
