package io.hhplus.tdd.domain.coupon.domain.model;

import io.hhplus.tdd.common.baseEntity.CreatedBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class UserCoupons extends CreatedBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType type;

    @Column(nullable = false)
    private int discountValue;

    @Column(nullable = false)
    private int minOrderAmount = 0;

    @Column(nullable = false)
    private Integer maxDiscountAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status; // UNUSED , USED , EXPIRED

    @Column
    private LocalDateTime usedAt;

    @Column
    private Long orderId; // 쿠폰 사용 주문 id

    @Column(nullable = false)
    private LocalDateTime validUntil; // 만기기간

    @Version
    private Long version; // 낙관락

    @Column
    private String issuanceId; // 멱등키

    public static UserCoupons from(long userId , Coupons coupon){
        return UserCoupons.builder()
                .userId(userId)
                .couponId(coupon.getId())
                .type(coupon.getType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .status(Status.UNUSED)
                .validUntil(LocalDateTime.now().plusDays(coupon.getDuration()))
                .build();
    }
//
//    public void useCoupon(){
//        validUseCoupon();
//        this.status = Status.USED;
//        this.usedAt = LocalDate.now();
//    }
//
//    public void restoreCoupon(){
//        this.status = Status.UNSUED;
//    }
//
//    public void validUseCoupon(){
//        if (LocalDate.now().isAfter(this.getValidUntil()) || this.status.equals(Status.EXPIRED)) {
//            throw new CouponException(ErrorCode.COUPON_USER_EXPIRED, this.getId());
//        }
//        if (this.status.equals(Status.USED)) {
//            throw new CouponException(ErrorCode.COUPON_USER_USED, this.getId());
//        }
//    }
}
