package io.hhplus.tdd.domain.coupon.infrastructure.repository;

import io.hhplus.tdd.domain.coupon.domain.model.Coupons;
import io.hhplus.tdd.domain.coupon.domain.model.UserCoupons;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponsRepository extends JpaRepository<UserCoupons, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 핵심: 비관적 락 (쓰기 락)
    @Query("select c from Coupons c where c.id = :id")
    Optional<Coupons> findByIdForUpdate(@Param("id") Long id);
}
