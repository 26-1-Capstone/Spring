package com.nutrishare.groupbuying.domain;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_purchases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class GroupPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId; // Creator ID (IAM Account ID)

    @Column(nullable = false)
    private Long productId; // Catalog Product ID

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer targetQuantity;

    @Column(nullable = false)
    private Integer currentQuantity;

    @Column(nullable = false)
    private Long unitPrice;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupPurchaseStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public GroupPurchase(Long ownerId, Long productId, String title, String description,
            Integer targetQuantity, Long unitPrice, LocalDateTime endAt) {
        if (endAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date must be in the future");
        }
        if (targetQuantity <= 0) {
            throw new IllegalArgumentException("Target quantity must be positive");
        }

        this.ownerId = ownerId;
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.targetQuantity = targetQuantity;
        this.unitPrice = unitPrice;
        this.endAt = endAt;
        this.currentQuantity = 0;
        this.status = GroupPurchaseStatus.OPEN;
    }

    public static GroupPurchase create(Long ownerId, Long productId, String title, String description,
            Integer targetQuantity, Long unitPrice, LocalDateTime endAt) {
        return new GroupPurchase(ownerId, productId, title, description, targetQuantity, unitPrice, endAt);
    }

    public void close() {
        if (this.status != GroupPurchaseStatus.OPEN) {
            throw new DomainException(ErrorCode.GROUP_CLOSED) {
                @Override
                public String getMessage() {
                    return "Group is already closed or canceled";
                }
            };
        }
        this.status = GroupPurchaseStatus.CLOSED;
    }

    public void cancel(Long requesterId) {
        if (!this.ownerId.equals(requesterId)) {
            throw new DomainException(ErrorCode.PERMISSION_DENIED) {
                @Override
                public String getMessage() {
                    return "Only owner can cancel";
                }
            };
        }
        if (this.status != GroupPurchaseStatus.OPEN) {
            throw new DomainException(ErrorCode.GROUP_CLOSED) {
            };
        }
        this.status = GroupPurchaseStatus.CANCELED;
    }

    public void registerParticipation(int quantity) {
        if (this.status != GroupPurchaseStatus.OPEN) {
            throw new DomainException(ErrorCode.GROUP_CLOSED) {
                @Override
                public String getMessage() {
                    return "Group is not open for participation";
                }
            };
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        this.currentQuantity += quantity;
        if (this.currentQuantity >= this.targetQuantity) {
            this.status = GroupPurchaseStatus.CLOSED;
        }
    }
}
