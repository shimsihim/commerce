package io.hhplus.tdd.domain.coupon.infrastructure.repository;

import io.hhplus.tdd.common.exception.CouponException;
import io.hhplus.tdd.common.exception.ErrorCode;
import io.hhplus.tdd.common.redis.RedisKeyBuilder;
import io.hhplus.tdd.domain.coupon.domain.model.CouponIssueInfo;
import io.hhplus.tdd.domain.coupon.domain.model.Coupons;
import io.hhplus.tdd.domain.coupon.domain.model.DiscountType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class CouponRedisRepository implements CouponStockRepository {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> issueScript;
    private final RedisKeyBuilder redisKeyBuilder;

    public CouponRedisRepository(StringRedisTemplate redisTemplate, RedisKeyBuilder redisKeyBuilder) {
        this.redisTemplate = redisTemplate;
        this.redisKeyBuilder = redisKeyBuilder;
        this.issueScript = new DefaultRedisScript<>();
        this.issueScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/coupon-issue.lua")));
        this.issueScript.setResultType(List.class);
    }

    @Override
    public void registerCoupon(Coupons coupon) {
        String key = redisKeyBuilder.couponInfo(coupon.getId());

        Map<String, String> hash = new HashMap<>();
        hash.put("couponId", String.valueOf(coupon.getId()));
        hash.put("totalQuantity", coupon.getTotalQuantity() == null ? "-1" : String.valueOf(coupon.getTotalQuantity()));
        hash.put("issuedQuantity", String.valueOf(coupon.getIssuedQuantity()));
        hash.put("maxIssuancePerUser", String.valueOf(coupon.getMaxIssuancePerUser()));
        hash.put("type", coupon.getType().name());
        hash.put("discountValue", String.valueOf(coupon.getDiscountValue()));
        hash.put("minOrderAmount", String.valueOf(coupon.getMinOrderAmount()));
        hash.put("maxDiscountAmount", coupon.getMaxDiscountAmount() == null ? "-1" : String.valueOf(coupon.getMaxDiscountAmount()));
        hash.put("duration", String.valueOf(coupon.getDuration()));

        redisTemplate.opsForHash().putAll(key, hash);

        Duration ttl = redisKeyBuilder.getCouponInfoTTL(coupon.getValidUntil());
        redisTemplate.expire(key, ttl);

        log.info("Registered coupon in Redis: couponId={}", coupon.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public CouponIssueInfo tryIssueCoupon(long couponId, long userId, String issuanceId) {
        log.warn("couponId : {} , userId : {} , issuanceId: {}" , couponId,userId,issuanceId);

        String couponKey = redisKeyBuilder.couponInfo(couponId);
        String userKey = redisKeyBuilder.userHistory(couponId , userId);

        List<String> result = redisTemplate.execute(
                issueScript,
                List.of(couponKey, userKey),
                issuanceId
        );

        if (result == null || result.isEmpty()) {
            throw new CouponException(ErrorCode.COUPON_NOT_FOUND, couponId);
        }

        String status = result.get(0);

        return switch (status) {
            case "DUPLICATE" -> throw new CouponException(ErrorCode.COUPON_DUPLICATE_REQUEST, couponId);
            case "NOT_FOUND" -> throw new CouponException(ErrorCode.COUPON_NOT_FOUND, couponId);
            case "SOLD_OUT" -> throw new CouponException(ErrorCode.COUPON_ISSUE_LIMIT, couponId);
            case "LIMIT_PER_USER" -> throw new CouponException(ErrorCode.COUPON_ISSUE_LIMIT_PER_USER, userId);
            case "SUCCESS" -> parseCouponIssueInfo(result);
            default -> throw new CouponException(ErrorCode.COUPON_NOT_FOUND, couponId);
        };
    }

    private CouponIssueInfo parseCouponIssueInfo(List<String> result) {
        String type = result.get(1);
        int discountValue = Integer.parseInt(result.get(2));
        int minOrderAmount = Integer.parseInt(result.get(3));
        String maxDiscountAmountStr = result.get(4);
        Integer maxDiscountAmount = "-1".equals(maxDiscountAmountStr) ? null : Integer.parseInt(maxDiscountAmountStr);
        int duration = Integer.parseInt(result.get(5));
        long couponId = Long.parseLong(result.get(6));

        return new CouponIssueInfo(
                couponId,
                DiscountType.valueOf(type),
                discountValue,
                minOrderAmount,
                maxDiscountAmount,
                duration
        );
    }
}
