package com.nutrishare.review.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reviews_participation", columnNames = "participationId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long participationId;

    @Column(nullable = false)
    private Long groupPurchaseId;

    @Column
    private Long hostId;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 120)
    private String comment;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Review(Long userId, Long participationId, Long groupPurchaseId, Long hostId, Integer rating, String comment) {
        validateTarget(groupPurchaseId, hostId);
        validateReview(rating, comment);
        this.userId = userId;
        this.participationId = participationId;
        this.groupPurchaseId = groupPurchaseId;
        this.hostId = hostId;
        this.rating = rating;
        this.comment = normalizeComment(comment);
    }

    public static Review create(Long userId, Long participationId, Long groupPurchaseId, Long hostId, Integer rating, String comment) {
        return new Review(userId, participationId, groupPurchaseId, hostId, rating, comment);
    }

    public void update(Integer rating, String comment) {
        validateReview(rating, comment);
        this.rating = rating;
        this.comment = normalizeComment(comment);
    }

    public void alignToHost(Long groupPurchaseId, Long hostId) {
        validateTarget(groupPurchaseId, hostId);
        this.groupPurchaseId = groupPurchaseId;
        this.hostId = hostId;
    }

    private static void validateTarget(Long groupPurchaseId, Long hostId) {
        if (groupPurchaseId == null || hostId == null) {
            throw new IllegalArgumentException("Group purchase and host are required");
        }
    }

    private static void validateReview(Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Comment is required");
        }

        if (normalizeComment(comment).length() > 120) {
            throw new IllegalArgumentException("Comment must be 120 characters or less");
        }
    }

    private static String normalizeComment(String comment) {
        return comment == null ? "" : comment.trim();
    }
}
