package com.nutrishare.infrastructure.configuration;

import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.infrastructure.persistence.GroupPurchaseJpaRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewHostMigrationInitializerTest {

    @Mock
    private ReviewJpaRepository reviewRepository;

    @Mock
    private GroupPurchaseJpaRepository groupPurchaseRepository;

    @InjectMocks
    private ReviewHostMigrationInitializer initializer;

    @Test
    void backfillsLegacyReviewsUsingGroupOwner() throws Exception {
        Review firstReview = Review.create(1L, 10L, 100L, 201L, 5, "좋아요");
        Review secondReview = Review.create(2L, 11L, 101L, 202L, 4, "만족해요");
        ReflectionTestUtils.setField(firstReview, "hostId", null);
        ReflectionTestUtils.setField(secondReview, "hostId", null);

        when(reviewRepository.findAllByHostIdIsNull()).thenReturn(List.of(firstReview, secondReview));
        when(groupPurchaseRepository.findAllById(anyList())).thenReturn(List.of(
                createGroupPurchase(100L, 201L),
                createGroupPurchase(101L, 202L)));

        initializer.run();

        ArgumentCaptor<List<Review>> migratedCaptor = ArgumentCaptor.forClass(List.class);
        verify(reviewRepository).saveAll(migratedCaptor.capture());

        assertThat(migratedCaptor.getValue())
                .extracting(Review::getHostId)
                .containsExactlyInAnyOrder(201L, 202L);
    }

    private GroupPurchase createGroupPurchase(Long groupPurchaseId, Long ownerId) {
        GroupPurchase groupPurchase = GroupPurchase.create(
                ownerId,
                11L,
                "테스트 공동구매",
                "설명",
                2,
                1000L,
                LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(groupPurchase, "id", groupPurchaseId);
        return groupPurchase;
    }
}
