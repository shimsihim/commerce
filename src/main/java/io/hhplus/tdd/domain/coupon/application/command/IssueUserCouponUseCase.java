package io.hhplus.tdd.domain.coupon.application.command;

import io.hhplus.tdd.domain.coupon.domain.model.CouponIssueInfo;
import io.hhplus.tdd.domain.coupon.domain.model.UserCoupons;
import io.hhplus.tdd.domain.coupon.infrastructure.repository.CouponStockRepository;
import io.hhplus.tdd.domain.coupon.infrastructure.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class IssueUserCouponUseCase {

    private final UserCouponRepository userCouponRepository;
    private final CouponStockRepository couponStockRepository;

    public record Input(
            long userId,
            long couponId,
            String issuanceId
    ) {
    }

    @Transactional
    public void execute(IssueUserCouponUseCase.Input input) {
        // STEP 1: 원자적 검증 + 예약 (재고 차감, 멱등키 확인, 유저별 한도)
        CouponIssueInfo couponIssueInfo = couponStockRepository.tryIssueCoupon(
                input.couponId(), input.userId(), input.issuanceId()
        );

        // STEP 2: DB 저장
        UserCoupons userCoupons = UserCoupons.from(input.userId(), couponIssueInfo, input.issuanceId());
        userCouponRepository.save(userCoupons);
    }

}
