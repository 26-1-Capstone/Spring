package com.nutrishare.ordering.infrastructure.persistence;

import com.nutrishare.ordering.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);
}
