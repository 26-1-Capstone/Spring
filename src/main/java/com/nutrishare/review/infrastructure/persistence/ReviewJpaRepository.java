package com.nutrishare.review.infrastructure.persistence;

import com.nutrishare.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByParticipationId(Long participationId);

    List<Review> findAllByParticipationIdIn(Collection<Long> participationIds);

    List<Review> findAllByHostIdIsNull();
}
