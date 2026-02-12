package io.hhplus.tdd.domain.coupon.application.query;

import io.hhplus.tdd.domain.coupon.domain.model.DiscountType;
import io.hhplus.tdd.domain.coupon.domain.model.Status;
import io.hhplus.tdd.domain.coupon.domain.model.UserCoupons;
import io.hhplus.tdd.domain.coupon.infrastructure.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class GetMyCouponListUseCase {

    private final UserCouponRepository userCouponRepository;

    public record Input(
            long userId
    ) {
    }

    @Transactional(readOnly = true)
    public List<Output> execute(Input input) {

        long userId = input.userId();
        Status status = Status.UNUSED;
        LocalDateTime now = LocalDateTime.now();

        return userCouponRepository.findAllByUserIdAndStatusAndValidUntilAfter(userId ,status , now)
                .stream()
                .map(Output::from)
                .toList();
    }


    public record Output(
            long id,
            long userId,
            long couponId,
            DiscountType type,
            Status status,
            LocalDateTime usedAt,
            int discountValue,
            int minOrderAmount,
            String maxDiscountAmount,
            LocalDateTime validUntil
    ) {
        public static Output from(UserCoupons userCoupon) {

            String maxDiscountAmount = userCoupon.getMaxDiscountAmount() == null ? "제한없음" : userCoupon.getMaxDiscountAmount().toString();

            return new Output(
                    userCoupon.getId(),
                    userCoupon.getUserId(),
                    userCoupon.getCouponId(),
                    userCoupon.getType(),
                    userCoupon.getStatus(),
                    userCoupon.getUsedAt(),
                    userCoupon.getDiscountValue(),
                    userCoupon.getMinOrderAmount(),
                    maxDiscountAmount,
                    userCoupon.getValidUntil()
            );
        }
    }

}
