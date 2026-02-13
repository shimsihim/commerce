package io.hhplus.tdd.common.redis;

import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDateTime;

@Component("redisKey") // SpEL에서 @redisKey 로 호출
public class RedisKeyBuilder {

    // SpEL: @redisKey.couponInfo(#couponId)
    public String couponInfo(Long couponId) {
        return Policy.COUPON_INFO.build(couponId);
    }

    // SpEL: @redisKey.userHistory(#couponId, #userId)
    public String userHistory(Long couponId, Long userId) {
        return Policy.USER_COUPON_HISTORY.build(couponId, userId);
    }

    public Duration getCouponInfoTTL(LocalDateTime validUntil) {
        if (validUntil == null) {
            return Policy.COUPON_INFO.ttl;
        }
        // 현재부터 (유효기간 + 3일) 까지의 시간 차이 계산
        Duration duration = Duration.between(LocalDateTime.now(), validUntil.plusDays(3));

        // 이미 지난 날짜라면 최소 1시간은 보장 (즉시 삭제 방지)
        return duration.isNegative() ? Duration.ofHours(1) : duration;
    }


    @Getter
    @AllArgsConstructor
    private enum Policy {

        //레디스 캐시
        //쿠폰 및 사용자 별 쿠폰 이력은 쿠폰의 validUntil + 3일을 따라감
        COUPON_INFO("coupon:%s:info", Duration.ofDays(7)),
        USER_COUPON_HISTORY("coupon:%s:user:%s", Duration.ofDays(7));

        // 분산락
//        LOCK_COUPON_ISSUE("lock:coupon:issue:%s", Duration.ofSeconds(10));

        private final String pattern;
        private final Duration ttl;

        public String build(Object... args) {
            return String.format(pattern, args);
        }
    }
}