package io.hhplus.tdd.domain.coupon.application.command;

import io.hhplus.tdd.common.exception.CouponException;
import io.hhplus.tdd.common.exception.ErrorCode;
import io.hhplus.tdd.domain.coupon.domain.model.Coupons;
import io.hhplus.tdd.domain.coupon.domain.model.UserCoupons;
import io.hhplus.tdd.domain.coupon.infrastructure.repository.CouponsRepository;
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
    private final CouponsRepository couponsRepository;

    public record Input(
            long userId,
            long couponId
    ) {
    }

    @Transactional
    public void execute(IssueUserCouponUseCase.Input input) {
        Coupons coupon = couponsRepository.findByIdForUpdate(input.couponId())
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND, input.userId(), input.couponId()));

        long userCouponCnt = userCouponRepository.countByUserIdAndCouponId(input.userId(),input.couponId());
        //1. 쿠폰 발급 가능 검증
        coupon.validIssue(userCouponCnt , input.userId());

        //2. 쿠폰 개수 -
        coupon.increaseIssuedQuantity();

        //3. 사용자 쿠폰 추가
        UserCoupons userCoupons = UserCoupons.from(input.userId() , coupon);
        userCouponRepository.save(userCoupons);
    }

}
