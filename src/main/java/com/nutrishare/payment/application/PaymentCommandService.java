package com.nutrishare.payment.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import com.nutrishare.ordering.domain.Order;
import com.nutrishare.ordering.domain.OrderRepository;
import com.nutrishare.payment.domain.Payment;
import com.nutrishare.payment.domain.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Long processPayment(Long userId, Long orderId, Long amount, String provider, String providerKey) {
        // Idempotency Check
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new DomainException(ErrorCode.INVALID_REQUEST) {
                @Override
                public String getMessage() {
                    return "Payment already exists for this order";
                }
            };
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Order not found";
                    }
                });

        if (!order.getUserId().equals(userId)) {
            throw new DomainException(ErrorCode.PERMISSION_DENIED) {
                @Override
                public String getMessage() {
                    return "Not owner of order";
                }
            };
        }

        if (!order.getTotalAmount().equals(amount)) {
            throw new DomainException(ErrorCode.INVALID_REQUEST) {
                @Override
                public String getMessage() {
                    return "Payment amount does not match order total";
                }
            };
        }

        Payment payment = Payment.create(orderId, userId, amount, provider);
        payment.confirm(providerKey); // Simultaneously confirm for MVP (assuming trusted source)
        order.pay();
        order.paid();

        // Use ApplicationEventPublisher here to notify Order Context
        // eventPublisher.publish(new PaymentConfirmedEvent(orderId));

        return paymentRepository.save(payment).getId();
    }
}
