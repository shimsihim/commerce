package io.hhplus.tdd.domain.coupon.domain.model;

public record CouponIssueInfo(
        long couponId,
        DiscountType type,
        int discountValue,
        int minOrderAmount,
        Integer maxDiscountAmount,
        int duration
) {
}
