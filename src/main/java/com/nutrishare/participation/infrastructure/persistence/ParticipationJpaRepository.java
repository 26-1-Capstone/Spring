package com.nutrishare.participation.infrastructure.persistence;

import com.nutrishare.participation.domain.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationJpaRepository extends JpaRepository<Participation, Long> {
    boolean existsByUserIdAndGroupPurchaseId(Long userId, Long groupPurchaseId);

    List<Participation> findAllByUserId(Long userId);
}
