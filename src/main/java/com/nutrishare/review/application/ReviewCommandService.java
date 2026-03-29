package com.nutrishare.review.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.domain.GroupPurchaseStatus;
import com.nutrishare.groupbuying.infrastructure.persistence.GroupPurchaseJpaRepository;
import com.nutrishare.participation.domain.Participation;
import com.nutrishare.participation.domain.ParticipationStatus;
import com.nutrishare.participation.infrastructure.persistence.ParticipationJpaRepository;
import com.nutrishare.review.domain.Review;
import com.nutrishare.review.infrastructure.persistence.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandService {

    private final ReviewJpaRepository reviewRepository;
    private final ParticipationJpaRepository participationRepository;
    private final GroupPurchaseJpaRepository groupPurchaseRepository;

    public Long upsertReview(Long userId, Long participationId, Integer rating, String comment) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Participation not found";
                    }
                });

        if (!participation.getUserId().equals(userId)) {
            throw new DomainException(ErrorCode.PERMISSION_DENIED) {
                @Override
                public String getMessage() {
                    return "Not owner of participation";
                }
            };
        }

        GroupPurchase groupPurchase = groupPurchaseRepository.findById(participation.getGroupPurchaseId())
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Group purchase not found";
                    }
                });

        if (!isReviewEligible(participation, groupPurchase)) {
            throw new DomainException(ErrorCode.INVALID_REQUEST) {
                @Override
                public String getMessage() {
                    return "Review can only be written after the group purchase is completed";
                }
            };
        }

        Review review = reviewRepository.findByParticipationId(participationId)
                .map(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        throw new DomainException(ErrorCode.PERMISSION_DENIED) {
                            @Override
                            public String getMessage() {
                                return "Not owner of review";
                            }
                        };
                    }
                    existing.alignToHost(groupPurchase.getId(), groupPurchase.getOwnerId());
                    existing.update(rating, comment);
                    return existing;
                })
                .orElseGet(() -> Review.create(
                        userId,
                        participationId,
                        participation.getGroupPurchaseId(),
                        groupPurchase.getOwnerId(),
                        rating,
                        comment));

        return reviewRepository.save(review).getId();
    }

    private boolean isReviewEligible(Participation participation, GroupPurchase groupPurchase) {
        boolean completedGroup = groupPurchase.getStatus() == GroupPurchaseStatus.CLOSED;
        boolean completedParticipation = participation.getStatus() == ParticipationStatus.ACCEPTED
                || participation.getStatus() == ParticipationStatus.ORDERED;
        return completedGroup && completedParticipation;
    }
}
