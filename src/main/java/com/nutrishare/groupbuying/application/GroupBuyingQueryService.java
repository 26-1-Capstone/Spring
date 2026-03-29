package com.nutrishare.groupbuying.application;

import com.nutrishare.catalog.domain.Product;
import com.nutrishare.catalog.infrastructure.persistence.ProductJpaRepository;
import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.infrastructure.persistence.GroupPurchaseJpaRepository;
import com.nutrishare.iam.infrastructure.persistence.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupBuyingQueryService {

    private final GroupPurchaseJpaRepository groupPurchaseRepository;
    private final ProductJpaRepository productRepository;
    private final AccountJpaRepository accountRepository;

    public Page<GroupPurchaseSummaryView> getGroupPurchases(Pageable pageable) {
        return groupPurchaseRepository.findAll(pageable)
                .map(this::toSummaryView);
    }

    public GroupPurchaseDetailView getGroupPurchaseDetail(Long id) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Group Purchase not found";
                    }
                });

        return toDetailView(groupPurchase);
    }

    private GroupPurchaseSummaryView toSummaryView(GroupPurchase gp) {
        String productName = productRepository.findById(gp.getProductId())
                .map(Product::getName)
                .orElse("알 수 없는 상품");

        return new GroupPurchaseSummaryView(
                gp.getId(),
                gp.getTitle(),
                productName,
                gp.getUnitPrice(),
                gp.getCurrentQuantity(),
                gp.getTargetQuantity(),
                gp.getEndAt(),
                gp.getStatus().name(),
                extractDong(gp.getOwnerId()));
    }

    private GroupPurchaseDetailView toDetailView(GroupPurchase gp) {
        String productName = productRepository.findById(gp.getProductId())
                .map(Product::getName)
                .orElse("알 수 없는 상품");

        return new GroupPurchaseDetailView(
                gp.getId(),
                gp.getTitle(),
                gp.getDescription(),
                productName,
                gp.getUnitPrice(),
                gp.getCurrentQuantity(),
                gp.getTargetQuantity(),
                gp.getEndAt(),
                gp.getStatus().name(),
                gp.getOwnerId(),
                extractDong(gp.getOwnerId()));
    }

    private String extractDong(Long ownerId) {
        return accountRepository.findById(ownerId)
                .map(account -> account.getAddress() != null ? account.getAddress().getDong() : null)
                .orElse(null);
    }

    public record GroupPurchaseSummaryView(
            Long id,
            String title,
            String productName,
            Long typePrice,
            Integer currentQuantity,
            Integer targetQuantity,
            LocalDateTime dueDate,
            String status,
            String dong) {
    }

    public record GroupPurchaseDetailView(
            Long id,
            String title,
            String description,
            String productName,
            Long typePrice,
            Integer currentQuantity,
            Integer targetQuantity,
            LocalDateTime dueDate,
            String status,
            Long ownerId,
            String dong) {
    }
}
