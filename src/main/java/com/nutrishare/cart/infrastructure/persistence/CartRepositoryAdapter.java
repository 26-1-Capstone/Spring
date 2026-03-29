package com.nutrishare.cart.infrastructure.persistence;

import com.nutrishare.cart.domain.Cart;
import com.nutrishare.cart.domain.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository jpaRepository;

    @Override
    public Cart save(Cart cart) {
        return jpaRepository.save(cart);
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }
}
