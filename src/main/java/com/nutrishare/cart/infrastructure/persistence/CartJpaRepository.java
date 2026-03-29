package com.nutrishare.cart.infrastructure.persistence;

import com.nutrishare.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}
