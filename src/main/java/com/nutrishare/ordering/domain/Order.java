package com.nutrishare.ordering.domain;

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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items = new ArrayList<>();

    // Address as VO or Embedded? Spec says VO. For simplicity, plain fields first
    // or simple Embeddable.
    // Spec: "shippingAddress": { "zip": "...", "line1": "...", "line2": "..." }
    @Embedded
    private ShippingAddress shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Long totalAmount;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Order(Long userId, ShippingAddress shippingAddress, List<OrderItem> items) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Items cannot be empty");
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.status = OrderStatus.CREATED;
        this.totalAmount = items.stream().mapToLong(OrderItem::getTotalPrice).sum();
    }

    public static Order create(Long userId, ShippingAddress shippingAddress, List<OrderItem> items) {
        return new Order(userId, shippingAddress, items);
    }

    public void pay() {
        if (this.status != OrderStatus.CREATED) {
            throw new DomainException(ErrorCode.INVALID_REQUEST) {
                @Override
                public String getMessage() {
                    return "Order is not in CREATED state";
                }
            };
        }
        this.status = OrderStatus.PAYING;
    }

    public void paid() {
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (canCancel()) {
            this.status = OrderStatus.CANCELED;
        } else {
            throw new DomainException(ErrorCode.INVALID_REQUEST) {
                @Override
                public String getMessage() {
                    return "Cannot cancel current order status";
                }
            };
        }
    }

    private boolean canCancel() {
        return this.status == OrderStatus.CREATED || this.status == OrderStatus.PAYING; // Before PAID
        // If PAID, refund needed.
    }
}
