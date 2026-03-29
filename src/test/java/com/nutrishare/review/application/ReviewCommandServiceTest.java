package com.nutrishare.review.application;

import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.infrastructure.persistence.GroupPurchaseJpaRepository;
import com.nutrishare.participation.domain.Participation;
import com.nutrishare.participation.infrastructure.persistence.ParticipationJpaRepository;
import com.nutrishare.review.domain.Review;
import com.nutrishare.review.infrastructure.persistence.ReviewJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewCommandServiceTest {

    @Mock
    private ReviewJpaRepository reviewRepository;

    @Mock
    private ParticipationJpaRepository participationRepository;

    @Mock
    private GroupPurchaseJpaRepository groupPurchaseRepository;

    @InjectMocks
    private ReviewCommandService reviewCommandService;

    @Test
    void createsHostTargetedReviewForParticipation() {
        Participation participation = createAcceptedParticipation(7L, 31L, 1L);
        GroupPurchase groupPurchase = createClosedGroupPurchase(31L, 99L);

        when(participationRepository.findById(7L)).thenReturn(Optional.of(participation));
        when(groupPurchaseRepository.findById(31L)).thenReturn(Optional.of(groupPurchase));
        when(reviewRepository.findByParticipationId(7L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            ReflectionTestUtils.setField(review, "id", 501L);
            return review;
        });

        Long reviewId = reviewCommandService.upsertReview(1L, 7L, 5, "개설자가 친절했어요.");

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());

        assertThat(reviewId).isEqualTo(501L);
        assertThat(reviewCaptor.getValue().getParticipationId()).isEqualTo(7L);
        assertThat(reviewCaptor.getValue().getGroupPurchaseId()).isEqualTo(31L);
        assertThat(reviewCaptor.getValue().getHostId()).isEqualTo(99L);
    }

    @Test
    void backfillsMissingHostTargetWhenUpdatingExistingReview() {
        Participation participation = createAcceptedParticipation(7L, 31L, 1L);
        GroupPurchase groupPurchase = createClosedGroupPurchase(31L, 99L);
        Review legacyReview = Review.create(1L, 7L, 31L, 99L, 3, "예전 후기");
        ReflectionTestUtils.setField(legacyReview, "hostId", null);

        when(participationRepository.findById(7L)).thenReturn(Optional.of(participation));
        when(groupPurchaseRepository.findById(31L)).thenReturn(Optional.of(groupPurchase));
        when(reviewRepository.findByParticipationId(7L)).thenReturn(Optional.of(legacyReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewCommandService.upsertReview(1L, 7L, 4, "호스트 응답이 빨랐어요.");

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());

        assertThat(reviewCaptor.getValue().getHostId()).isEqualTo(99L);
        assertThat(reviewCaptor.getValue().getComment()).isEqualTo("호스트 응답이 빨랐어요.");
        assertThat(reviewCaptor.getValue().getRating()).isEqualTo(4);
    }

    private Participation createAcceptedParticipation(Long participationId, Long groupPurchaseId, Long userId) {
        Participation participation = Participation.create(userId, groupPurchaseId, 1);
        participation.accept();
        ReflectionTestUtils.setField(participation, "id", participationId);
        return participation;
    }

    private GroupPurchase createClosedGroupPurchase(Long groupPurchaseId, Long ownerId) {
        GroupPurchase groupPurchase = GroupPurchase.create(
                ownerId,
                11L,
                "테스트 공동구매",
                "설명",
                1,
                1000L,
                LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(groupPurchase, "id", groupPurchaseId);
        groupPurchase.registerParticipation(1);
        return groupPurchase;
    }
}
