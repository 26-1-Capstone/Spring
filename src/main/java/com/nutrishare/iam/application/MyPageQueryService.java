package com.nutrishare.iam.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.infrastructure.persistence.AccountJpaRepository;
import com.nutrishare.catalog.domain.Product;
import com.nutrishare.catalog.infrastructure.persistence.ProductJpaRepository;
import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.domain.GroupPurchaseStatus;
import com.nutrishare.groupbuying.infrastructure.persistence.GroupPurchaseJpaRepository;
import com.nutrishare.ordering.domain.Order;
import com.nutrishare.ordering.infrastructure.persistence.OrderJpaRepository;
import com.nutrishare.participation.domain.Participation;
import com.nutrishare.participation.domain.ParticipationStatus;
import com.nutrishare.participation.infrastructure.persistence.ParticipationJpaRepository;
import com.nutrishare.review.domain.Review;
import com.nutrishare.review.infrastructure.persistence.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageQueryService {

    private final AccountJpaRepository accountRepository;
    private final OrderJpaRepository orderRepository;
    private final ParticipationJpaRepository participationRepository;
    private final GroupPurchaseJpaRepository groupPurchaseRepository;
    private final ProductJpaRepository productRepository;
    private final ReviewJpaRepository reviewRepository;

    public UserProfileView getUserProfile(Long userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "User not found";
                    }
                });

        return new UserProfileView(
                account.getId(),
                account.getEmail(),
                account.getNickname(),
                account.getAddress());
    }

    public List<OrderSummaryView> getOrderHistory(Long userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(order -> new OrderSummaryView(
                        order.getId(),
                        buildOrderSummary(order),
                        order.getStatus().name(),
                        order.getTotalAmount(),
                        order.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<ParticipationSummaryView> getParticipationHistory(Long userId) {
        List<Participation> participations = participationRepository.findAllByUserId(userId);
        Map<Long, GroupPurchase> groupPurchasesById = groupPurchaseRepository.findAllById(
                        participations.stream().map(Participation::getGroupPurchaseId).distinct().toList()).stream()
                .collect(Collectors.toMap(GroupPurchase::getId, Function.identity()));
        Map<Long, Product> productsById = productRepository.findAllById(
                        groupPurchasesById.values().stream().map(GroupPurchase::getProductId).distinct().toList()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Account> hostsById = accountRepository.findAllById(
                        groupPurchasesById.values().stream().map(GroupPurchase::getOwnerId).distinct().toList()).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        Map<Long, Review> reviewsByParticipationId = reviewRepository.findAllByParticipationIdIn(
                        participations.stream().map(Participation::getId).toList()).stream()
                .collect(Collectors.toMap(Review::getParticipationId, Function.identity()));

        return participations.stream()
                .map(participation -> toParticipationSummary(
                        participation,
                        groupPurchasesById.get(participation.getGroupPurchaseId()),
                        reviewsByParticipationId.get(participation.getId()),
                        productsById,
                        hostsById))
                .collect(Collectors.toList());
    }

    private String buildOrderSummary(Order order) {
        if (order.getItems().isEmpty()) {
            return "주문 상품 정보 없음";
        }

        String firstItemName = order.getItems().get(0).getProductName();
        int extraItemCount = order.getItems().size() - 1;
        if (extraItemCount <= 0) {
            return firstItemName;
        }
        return firstItemName + " 외 " + extraItemCount + "건";
    }

    private ParticipationSummaryView toParticipationSummary(
            Participation participation,
            GroupPurchase groupPurchase,
            Review review,
            Map<Long, Product> productsById,
            Map<Long, Account> hostsById) {
        Product product = groupPurchase != null ? productsById.get(groupPurchase.getProductId()) : null;
        Account host = groupPurchase != null ? hostsById.get(groupPurchase.getOwnerId()) : null;
        boolean reviewEligible = isReviewEligible(participation, groupPurchase);

        return new ParticipationSummaryView(
                participation.getId(),
                participation.getGroupPurchaseId(),
                groupPurchase != null ? groupPurchase.getTitle() : "공동구매 #" + participation.getGroupPurchaseId(),
                product != null ? product.getName() : "상품 정보 없음",
                participation.getQuantity(),
                groupPurchase != null ? groupPurchase.getCurrentQuantity() : participation.getQuantity(),
                groupPurchase != null ? groupPurchase.getTargetQuantity() : participation.getQuantity(),
                participation.getStatus().name(),
                groupPurchase != null ? groupPurchase.getStatus().name() : null,
                groupPurchase != null ? groupPurchase.getOwnerId() : review != null ? review.getHostId() : null,
                host != null ? host.getNickname() : null,
                reviewEligible,
                review != null,
                review != null ? review.getRating() : null,
                review != null ? review.getComment() : null,
                participation.getCreatedAt());
    }

    private boolean isReviewEligible(Participation participation, GroupPurchase groupPurchase) {
        if (groupPurchase == null) {
            return false;
        }

        boolean completedGroup = groupPurchase.getStatus() == GroupPurchaseStatus.CLOSED;
        boolean completedParticipation = participation.getStatus() == ParticipationStatus.ACCEPTED
                || participation.getStatus() == ParticipationStatus.ORDERED;
        return completedGroup && completedParticipation;
    }

    // DTOs (Inner records for simplicity or separate files if large)
    public record UserProfileView(Long userId, String email, String nickname,
            com.nutrishare.iam.domain.Address address) {
    }

    public record OrderSummaryView(Long orderId, String summary, String status, Long totalAmount,
            java.time.LocalDateTime orderDate) {
    }

    public record ParticipationSummaryView(Long participationId, Long groupPurchaseId, String title, String productName,
            Integer quantity, Integer currentQuantity, Integer targetQuantity, String status,
            String groupStatus, Long hostId, String hostNickname, boolean reviewEligible, boolean reviewed,
            Integer reviewRating, String reviewComment, java.time.LocalDateTime createdAt) {
    }
}
