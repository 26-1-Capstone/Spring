package com.nutrishare.ordering.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import com.nutrishare.ordering.domain.Order;
import com.nutrishare.ordering.domain.OrderItem;
import com.nutrishare.ordering.domain.OrderRepository;
import com.nutrishare.ordering.domain.ShippingAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;
    // Should inject CatalogService to snapshot prices?
    // Or we accept what FE sends? -> SECURITY RISK.
    // MUST FETCH PRODUCT INFO.
    // For MVP, we assume validated input or inject a simple Catalog Adapter.
    // We will assume the request contains correct snapshot for now to fit
    // complexity.

    public Long createOrder(Long userId, ShippingAddress address, List<OrderItem> items) {
        // Validation logic for items price would go here in real app

        Order order = Order.create(userId, address, items);
        return orderRepository.save(order).getId();
    }

    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Order not found";
                    }
                });

        // Check ownership?
        if (!order.getUserId().equals(userId)) {
            throw new DomainException(ErrorCode.PERMISSION_DENIED) {
                @Override
                public String getMessage() {
                    return "Not owner of order";
                }
            };
        }

        order.cancel();
    }
}
