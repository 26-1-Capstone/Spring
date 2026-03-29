package com.nutrishare.payment.domain;

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
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String paymentProvider; // e.g., TOSS, KAKAO

    private String providerPaymentKey; // External Key

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Payment(Long orderId, Long userId, Long amount, String paymentProvider) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentProvider = paymentProvider;
        this.status = PaymentStatus.INIT;
    }

    public static Payment create(Long orderId, Long userId, Long amount, String paymentProvider) {
        return new Payment(orderId, userId, amount, paymentProvider);
    }

    public void confirm(String providerPaymentKey) {
        if (this.status != PaymentStatus.INIT) {
            throw new DomainException(ErrorCode.INVALID_REQUEST) {
                @Override
                public String getMessage() {
                    return "Payment already processed";
                }
            };
        }
        this.providerPaymentKey = providerPaymentKey;
        this.status = PaymentStatus.CONFIRMED;
    }
}
