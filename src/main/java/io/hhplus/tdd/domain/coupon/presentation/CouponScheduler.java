package io.hhplus.tdd.domain.coupon.presentation;

import io.hhplus.tdd.domain.coupon.application.command.CouponCacheWarmingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final CouponCacheWarmingUseCase cacheWarmingUseCase;

    // 매 10분마다 실행 (비즈니스 요구사항에 따라 조정)
    @Scheduled(fixedRate = 600000)
    public void warmUpCouponCache() {
        log.info("쿠폰 캐시 워밍 스케줄러 시작");
        try {
            cacheWarmingUseCase.execute();
            log.info("쿠폰 캐시 워밍 스케줄러 완료");
        } catch (Exception e) {
            log.error("쿠폰 캐시 워밍 중 오류 발생: {}", e.getMessage());
        }
    }
}