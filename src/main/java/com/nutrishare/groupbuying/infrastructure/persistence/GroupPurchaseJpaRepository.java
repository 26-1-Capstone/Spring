package com.nutrishare.groupbuying.infrastructure.persistence;

import com.nutrishare.groupbuying.domain.GroupPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupPurchaseJpaRepository extends JpaRepository<GroupPurchase, Long> {
}
