package com.nutrishare.participation.domain;

import java.util.Optional;

public interface ParticipationRepository {
    Participation save(Participation participation);

    Optional<Participation> findById(Long id);

    boolean existsByUserIdAndGroupPurchaseId(Long userId, Long groupPurchaseId);
}
