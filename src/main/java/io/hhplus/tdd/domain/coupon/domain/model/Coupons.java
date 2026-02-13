package io.hhplus.tdd.domain.coupon.domain.model;

import io.hhplus.tdd.common.baseEntity.UpdatableBaseEntity;
import io.hhplus.tdd.common.exception.ErrorCode;
import io.hhplus.tdd.common.exception.CouponException;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Slf4j
@ToString
public class Coupons extends UpdatableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType type;

    @Column(nullable = false)
    private int discountValue;

    @Column(nullable = false)
    private int minOrderAmount;

    @Column(nullable = false)
    private Integer maxDiscountAmount;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int maxIssuancePerUser;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private int duration;

    public void increaseIssuedQuantity() {
        if(isCouponRemain()){
            this.issuedQuantity++;
            return;
        }
        throw new CouponException(ErrorCode.COUPON_ISSUE_LIMIT, this.getId());
    }

    //할인 계산
    public long calculateDiscountAmount(long orderAmount) {
        if (orderAmount < this.minOrderAmount) {
            return 0;
        }

        if (this.type == DiscountType.PERCENTAGE) {
            return (orderAmount * this.discountValue) / 100;
        } else {
            return this.discountValue;
        }
    }

    public void validIssue(long issuedUserCouponCnt , long userId) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.getValidFrom()) || now.isAfter(this.getValidUntil())) {
            throw new CouponException(ErrorCode.COUPON_DURATION_ERR, this.getId());
        }
        if (!isCouponRemain()) {
            throw new CouponException(ErrorCode.COUPON_ISSUE_LIMIT, this.getId());
        }
        if(this.maxIssuancePerUser <= issuedUserCouponCnt){
            throw new CouponException(ErrorCode.COUPON_ISSUE_LIMIT_PER_USER, userId);
        }
    }


    private boolean isCouponRemain(){
        return this.getTotalQuantity() == null || this.getTotalQuantity() > this.getIssuedQuantity();
    }
}
