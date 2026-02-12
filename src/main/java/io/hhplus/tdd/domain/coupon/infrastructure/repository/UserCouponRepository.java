package io.hhplus.tdd.domain.coupon.infrastructure.repository;

import io.hhplus.tdd.domain.coupon.domain.model.Status;
import io.hhplus.tdd.domain.coupon.domain.model.UserCoupons;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupons, Long> {
    //사용자 미사용 사용 가능 쿠폰 조회
    List<UserCoupons> findAllByUserIdAndStatusAndValidUntilAfter(Long userId , Status status, LocalDateTime now);

    //사용자별 특정 쿠폰 보유 개수
    long countByUserIdAndCouponId(Long userId, Long couponId);
}
