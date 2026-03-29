package com.nutrishare.infrastructure.configuration;

import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.infrastructure.persistence.GroupPurchaseJpaRepository;
import com.nutrishare.review.domain.Review;
import com.nutrishare.review.infrastructure.persistence.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewHostMigrationInitializer implements CommandLineRunner {

    private final ReviewJpaRepository reviewRepository;
    private final GroupPurchaseJpaRepository groupPurchaseRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Review> legacyReviews = reviewRepository.findAllByHostIdIsNull();
        if (legacyReviews.isEmpty()) {
            return;
        }

        Map<Long, Long> hostIdsByGroupId = groupPurchaseRepository.findAllById(
                        legacyReviews.stream().map(Review::getGroupPurchaseId).distinct().toList()).stream()
                .collect(Collectors.toMap(GroupPurchase::getId, GroupPurchase::getOwnerId));

        List<Review> migratedReviews = legacyReviews.stream()
                .filter(review -> hostIdsByGroupId.containsKey(review.getGroupPurchaseId()))
                .peek(review -> review.alignToHost(
                        review.getGroupPurchaseId(),
                        hostIdsByGroupId.get(review.getGroupPurchaseId())))
                .toList();

        if (!migratedReviews.isEmpty()) {
            reviewRepository.saveAll(migratedReviews);
        }

        int skippedCount = legacyReviews.size() - migratedReviews.size();
        if (skippedCount > 0) {
            log.warn("Backfilled host targets for {} legacy reviews; skipped {} without a matching group purchase",
                    migratedReviews.size(),
                    skippedCount);
            return;
        }

        log.info("Backfilled host targets for {} legacy reviews", migratedReviews.size());
    }
}
