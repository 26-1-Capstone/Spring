package com.nutrishare.groupbuying.domain;

import java.util.Optional;

public interface GroupPurchaseRepository {
    GroupPurchase save(GroupPurchase groupPurchase);

    Optional<GroupPurchase> findById(Long id);
    // Basic Query methods or we use QueryDSL for listing
}
