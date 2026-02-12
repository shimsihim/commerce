package io.hhplus.tdd.domain.coupon.domain.model;

import io.hhplus.tdd.common.baseEntity.UpdatableBaseEntity;
import io.hhplus.tdd.common.exception.ErrorCode;
import io.hhplus.tdd.common.exception.CouponException;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

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
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false)
    private int duration;

    public void increaseIssuedQuantity() {
        if(this.totalQuantity - this.issuedQuantity > 0){
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
        LocalDate now = LocalDate.now();
        if (now.isBefore(this.getValidFrom()) || now.isAfter(this.getValidUntil())) {
            throw new CouponException(ErrorCode.COUPON_DURATION_ERR, this.getId());
        }
        if (this.getTotalQuantity() != null && this.getIssuedQuantity() >= this.getTotalQuantity()) {
            throw new CouponException(ErrorCode.COUPON_ISSUE_LIMIT, this.getId());
        }
        if(this.maxIssuancePerUser <= issuedUserCouponCnt){
            throw new CouponException(ErrorCode.COUPON_ISSUE_LIMIT_PER_USER, userId);
        }
    }
}
