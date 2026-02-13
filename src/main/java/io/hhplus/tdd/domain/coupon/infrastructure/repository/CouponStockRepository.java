package io.hhplus.tdd.domain.coupon.infrastructure.repository;

import io.hhplus.tdd.domain.coupon.domain.model.CouponIssueInfo;
import io.hhplus.tdd.domain.coupon.domain.model.Coupons;

public interface CouponStockRepository {

    void registerCoupon(Coupons coupon);

    CouponIssueInfo tryIssueCoupon(long couponId, long userId, String issuanceId);
}
