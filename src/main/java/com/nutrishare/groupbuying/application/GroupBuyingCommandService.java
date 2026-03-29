package com.nutrishare.groupbuying.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.domain.GroupPurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupBuyingCommandService {

    private final GroupPurchaseRepository groupPurchaseRepository;

    // In a real scenario, we might want to check Product existence via a remote
    // Service or Adapter
    // private final CatalogService catalogService;

    public Long createGroupPurchase(Long userId, Long productId, String title, String description,
            Integer targetQuantity, Long unitPrice, LocalDateTime endAt) {

        // TODO: Verify product exists? (Can be skipped for MVP or assumed valid)

        GroupPurchase groupPurchase = GroupPurchase.create(
                userId, productId, title, description, targetQuantity, unitPrice, endAt);
        return groupPurchaseRepository.save(groupPurchase).getId();
    }

    public void cancelGroupPurchase(Long userId, Long groupPurchaseId) {
        GroupPurchase gp = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Group Purchase not found";
                    }
                });

        gp.cancel(userId);
        // save not needed with dirty checking, but safe to have
    }
}
