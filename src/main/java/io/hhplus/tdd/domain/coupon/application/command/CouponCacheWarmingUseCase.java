package io.hhplus.tdd.domain.coupon.application.command;

import io.hhplus.tdd.domain.coupon.domain.model.Coupons;
import io.hhplus.tdd.domain.coupon.infrastructure.repository.CouponRedisRepository;
import io.hhplus.tdd.domain.coupon.infrastructure.repository.CouponsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponCacheWarmingUseCase {

    private final CouponsRepository couponsRepository;           // DB 조회용
    private final CouponRedisRepository couponRedisRepository; // Redis 등록용

    @Transactional(readOnly = true)
    public void execute() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime from = now.plusMinutes(5);
        LocalDateTime to = now.plusHours(1);

        List<Coupons> activeCoupons = couponsRepository.findAllByValidFromBetween(from , to);
        log.warn("활성 쿠폰 : {}" , activeCoupons);
        log.warn("활성 쿠폰 개수: {}" , activeCoupons.size());
        for (Coupons coupon : activeCoupons) {
            couponRedisRepository.registerCoupon(coupon);
        }
    }
}