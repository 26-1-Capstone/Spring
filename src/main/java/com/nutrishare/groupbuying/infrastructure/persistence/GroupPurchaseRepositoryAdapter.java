package com.nutrishare.groupbuying.infrastructure.persistence;

import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.domain.GroupPurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupPurchaseRepositoryAdapter implements GroupPurchaseRepository {

    private final GroupPurchaseJpaRepository jpaRepository;

    @Override
    public GroupPurchase save(GroupPurchase groupPurchase) {
        return jpaRepository.save(groupPurchase);
    }

    @Override
    public Optional<GroupPurchase> findById(Long id) {
        return jpaRepository.findById(id);
    }
}
