package com.nutrishare.participation.domain;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "participations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long groupPurchaseId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Participation(Long userId, Long groupPurchaseId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.userId = userId;
        this.groupPurchaseId = groupPurchaseId;
        this.quantity = quantity;
        this.status = ParticipationStatus.REQUESTED;
    }

    public static Participation create(Long userId, Long groupPurchaseId, Integer quantity) {
        return new Participation(userId, groupPurchaseId, quantity);
    }

    public void accept() {
        if (this.status != ParticipationStatus.REQUESTED) {
            // Might already be accepted
            throw new IllegalStateException("Already handled");
        }
        this.status = ParticipationStatus.ACCEPTED;
    }

    public void cancel(Long requesterId) {
        if (!this.userId.equals(requesterId)) {
            throw new DomainException(ErrorCode.PERMISSION_DENIED) {
                @Override
                public String getMessage() {
                    return "Access denied";
                }
            };
        }
        // Logic check: Can we cancel if already ordered? No.
        if (this.status == ParticipationStatus.ORDERED) {
            throw new DomainException(ErrorCode.INVALID_REQUEST) {
                @Override
                public String getMessage() {
                    return "Cannot cancel ordered participation";
                }
            };
        }
        this.status = ParticipationStatus.CANCELED;
    }
}
