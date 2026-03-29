package com.nutrishare.infrastructure.configuration;

import com.nutrishare.catalog.domain.Category;
import com.nutrishare.catalog.domain.Product;
import com.nutrishare.catalog.infrastructure.persistence.CategoryJpaRepository;
import com.nutrishare.catalog.infrastructure.persistence.ProductJpaRepository;
import com.nutrishare.groupbuying.domain.GroupPurchase;
import com.nutrishare.groupbuying.infrastructure.persistence.GroupPurchaseJpaRepository;
import com.nutrishare.iam.domain.Account;
import com.nutrishare.iam.domain.Address;
import com.nutrishare.iam.infrastructure.persistence.AccountJpaRepository;
import com.nutrishare.ordering.domain.Order;
import com.nutrishare.ordering.domain.OrderItem;
import com.nutrishare.ordering.domain.ShippingAddress;
import com.nutrishare.ordering.infrastructure.persistence.OrderJpaRepository;
import com.nutrishare.participation.domain.Participation;
import com.nutrishare.participation.infrastructure.persistence.ParticipationJpaRepository;
import com.nutrishare.payment.domain.Payment;
import com.nutrishare.payment.infrastructure.persistence.PaymentJpaRepository;
import com.nutrishare.review.domain.Review;
import com.nutrishare.review.infrastructure.persistence.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.qa.seed-enabled", havingValue = "true")
public class QaSeedDataInitializer implements CommandLineRunner {

    private final AccountJpaRepository accountRepository;
    private final CategoryJpaRepository categoryRepository;
    private final ProductJpaRepository productRepository;
    private final GroupPurchaseJpaRepository groupPurchaseRepository;
    private final ParticipationJpaRepository participationRepository;
    private final OrderJpaRepository orderRepository;
    private final PaymentJpaRepository paymentRepository;
    private final ReviewJpaRepository reviewRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0 || groupPurchaseRepository.count() > 0 || orderRepository.count() > 0) {
            log.info("Skipping QA seed data initialization because data already exists.");
            return;
        }

        Account devAccount = createAccount("dev@nutrishare.local", "개발자QA", "06236", "서울 강남구 테헤란로 152", "3층", "역삼동");
        Account sameDongHost = createAccount("host1@nutrishare.local", "역삼동장보기", "06236", "서울 강남구 테헤란로 160", "101호", "역삼동");
        Account otherDongHost = createAccount("host2@nutrishare.local", "서초동알뜰장", "06611", "서울 서초구 서초대로 77", "8층", "서초동");

        Category fresh = categoryRepository.save(new Category("신선식품"));
        Category pantry = categoryRepository.save(new Category("상온식품"));
        Category daily = categoryRepository.save(new Category("생활용품"));

        Product strawberries = productRepository.save(Product.create(fresh, "딸기 1kg", "같이 사서 택배로 나눠 받는 신선 딸기", 18900L, 100));
        Product ramen = productRepository.save(Product.create(pantry, "진라면 20봉", "집에 두고 먹기 좋은 대용량 라면 세트", 12800L, 200));
        Product tissue = productRepository.save(Product.create(daily, "두루마리 휴지 30롤", "생활비 절약용 공동구매 휴지", 21900L, 80));
        Product apple = productRepository.save(Product.create(fresh, "사과 5kg", "이웃과 함께 사는 가정용 사과 박스", 25900L, 60));

        GroupPurchase localOpenGroup = groupPurchaseRepository.save(GroupPurchase.create(
                sameDongHost.getId(),
                strawberries.getId(),
                "역삼동 새벽딸기 같이 사요",
                "이번 주말 전에 같이 주문해서 택배로 받아요.",
                8,
                16900L,
                LocalDateTime.now().plusDays(3)));
        localOpenGroup.registerParticipation(3);

        GroupPurchase localClosedReviewedGroup = groupPurchaseRepository.save(GroupPurchase.create(
                sameDongHost.getId(),
                ramen.getId(),
                "역삼동 라면 공동구매 마감분",
                "마감 완료된 QA용 공동구매입니다.",
                5,
                9800L,
                LocalDateTime.now().plusDays(1)));
        localClosedReviewedGroup.registerParticipation(5);

        GroupPurchase localClosedPendingReviewGroup = groupPurchaseRepository.save(GroupPurchase.create(
                sameDongHost.getId(),
                tissue.getId(),
                "역삼동 휴지 공동구매 후기 남기기",
                "리뷰 작성 테스트용으로 마감된 공동구매입니다.",
                4,
                18400L,
                LocalDateTime.now().plusDays(1)));
        localClosedPendingReviewGroup.registerParticipation(4);

        GroupPurchase otherDongOpenGroup = groupPurchaseRepository.save(GroupPurchase.create(
                otherDongHost.getId(),
                apple.getId(),
                "서초동 사과 나눔 공구",
                "다른 동네 공구가 섞여 보이는지 확인하기 위한 데이터입니다.",
                6,
                22900L,
                LocalDateTime.now().plusDays(5)));
        otherDongOpenGroup.registerParticipation(2);

        Participation reviewedParticipation = participationRepository.save(createAcceptedParticipation(
                devAccount.getId(),
                localClosedReviewedGroup.getId(),
                2));

        Participation pendingReviewParticipation = participationRepository.save(createAcceptedParticipation(
                devAccount.getId(),
                localClosedPendingReviewGroup.getId(),
                1));

        Participation openParticipation = participationRepository.save(createAcceptedParticipation(
                devAccount.getId(),
                localOpenGroup.getId(),
                1));

        Order paidOrder = orderRepository.save(createPaidOrder(
                devAccount.getId(),
                strawberries,
                tissue,
                devAccount.getAddress()));
        paymentRepository.save(createConfirmedPayment(devAccount.getId(), paidOrder.getId(), paidOrder.getTotalAmount()));

        reviewRepository.save(Review.create(
                devAccount.getId(),
                reviewedParticipation.getId(),
                reviewedParticipation.getGroupPurchaseId(),
                localClosedReviewedGroup.getOwnerId(),
                5,
                "같은 동네라 일정 맞추기 편했고 상품 상태도 좋았어요."));

        // Keep these variables referenced for readability in logs/debugging.
        log.info("QA seed data created: devAccount={}, groups={}, participations={}, pendingReviewParticipation={}, openParticipation={}",
                devAccount.getEmail(),
                List.of(localOpenGroup.getId(), localClosedReviewedGroup.getId(), localClosedPendingReviewGroup.getId(), otherDongOpenGroup.getId()),
                List.of(reviewedParticipation.getId(), pendingReviewParticipation.getId(), openParticipation.getId()),
                pendingReviewParticipation.getId(),
                openParticipation.getId());
    }

    private Account createAccount(String email, String nickname, String zipCode, String line1, String line2, String dong) {
        Account account = accountRepository.findByEmail(email)
                .orElseGet(() -> accountRepository.save(Account.create(email, nickname)));
        account.updateProfile(nickname, Address.of(zipCode, line1, line2, dong));
        return account;
    }

    private Participation createAcceptedParticipation(Long userId, Long groupPurchaseId, int quantity) {
        Participation participation = Participation.create(userId, groupPurchaseId, quantity);
        participation.accept();
        return participation;
    }

    private Order createPaidOrder(Long userId, Product firstProduct, Product secondProduct, Address address) {
        Order order = Order.create(
                userId,
                new ShippingAddress(address.getZipCode(), address.getAddressLine1(), address.getAddressLine2()),
                List.of(
                        new OrderItem(firstProduct.getId(), firstProduct.getName(), firstProduct.getPrice(), 1),
                        new OrderItem(secondProduct.getId(), secondProduct.getName(), secondProduct.getPrice(), 1)));
        order.pay();
        order.paid();
        return order;
    }

    private Payment createConfirmedPayment(Long userId, Long orderId, Long amount) {
        Payment payment = Payment.create(orderId, userId, amount, "SIMULATED");
        payment.confirm("seed-payment-" + orderId);
        return payment;
    }
}
