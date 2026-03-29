package com.nutrishare.cart.application;

import com.nutrishare.cart.domain.Cart;
import com.nutrishare.cart.domain.CartItem;
import com.nutrishare.cart.domain.CartRepository;
import com.nutrishare.catalog.domain.Product;
import com.nutrishare.catalog.infrastructure.persistence.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartQueryService {

    private final CartRepository cartRepository;
    private final ProductJpaRepository productRepository;

    public CartView getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);

        if (cart == null || cart.getItems().isEmpty()) {
            return new CartView(cart != null ? cart.getId() : null, Collections.emptyList(), 0L);
        }

        List<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .toList();

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<CartItemView> itemViews = cart.getItems().stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    String productName = product != null ? product.getName() : "Unknown Product";
                    Long price = product != null ? product.getPrice() : 0L;
                    return new CartItemView(
                            item.getProductId(),
                            productName,
                            price,
                            item.getQuantity(),
                            price * item.getQuantity());
                })
                .collect(Collectors.toList());

        Long totalAmount = itemViews.stream().mapToLong(CartItemView::totalPrice).sum();

        return new CartView(cart.getId(), itemViews, totalAmount);
    }

    public record CartView(Long cartId, List<CartItemView> items, Long totalAmount) {
    }

    public record CartItemView(Long productId, String productName, Long typePrice, Integer quantity, Long totalPrice) {
    }
}
