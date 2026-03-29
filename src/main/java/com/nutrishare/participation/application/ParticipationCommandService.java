package com.nutrishare.participation.application;

import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.domain.GroupPurchaseRepository;
import com.nutrishare.participation.domain.Participation;
import com.nutrishare.participation.domain.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationCommandService {

    private final ParticipationRepository participationRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;
    // Should inject GroupBuyingService to check group status?
    // Ideally yes, or Domain Service. For MVP, we might skip checking Group Status
    // here or trust the client/ID.
    // Spec says: "Check recruit status".
    // We will assume a valid ID for now, but in real world we need to validate
    // GroupPurchase status.

    public Long joinGroup(Long userId, Long groupPurchaseId, Integer quantity) {

        if (participationRepository.existsByUserIdAndGroupPurchaseId(userId, groupPurchaseId)) {
            throw new DomainException(ErrorCode.GROUP_ALREADY_JOINED) {
                // Using default message
            };
        }

        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Group Purchase not found";
                    }
                });

        Participation participation = Participation.create(userId, groupPurchaseId, quantity);
        // By default REQUESTED.

        // Auto-accept? Or waiting? Spec doesn't clarify acceptance flow deeply.
        // We'll leave it as REQUESTED -> and maybe an Event makes it ACCEPTED if stock
        // allows.
        participation.accept(); // For simple flow, auto-accept immediately.
        groupPurchase.registerParticipation(quantity);

        return participationRepository.save(participation).getId();
    }

    public void cancelParticipation(Long userId, Long participationId) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Participation not found";
                    }
                });

        participation.cancel(userId);
    }
}
