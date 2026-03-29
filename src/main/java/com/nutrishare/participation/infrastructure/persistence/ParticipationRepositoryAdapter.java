package com.nutrishare.participation.infrastructure.persistence;

import com.nutrishare.participation.domain.Participation;
import com.nutrishare.participation.domain.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ParticipationRepositoryAdapter implements ParticipationRepository {

    private final ParticipationJpaRepository jpaRepository;

    @Override
    public Participation save(Participation participation) {
        return jpaRepository.save(participation);
    }

    @Override
    public Optional<Participation> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsByUserIdAndGroupPurchaseId(Long userId, Long groupPurchaseId) {
        return jpaRepository.existsByUserIdAndGroupPurchaseId(userId, groupPurchaseId);
    }
}
