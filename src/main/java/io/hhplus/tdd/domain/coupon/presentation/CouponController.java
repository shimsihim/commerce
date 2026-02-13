package io.hhplus.tdd.domain.coupon.presentation;

import io.hhplus.tdd.domain.coupon.application.command.CouponCacheWarmingUseCase;
import io.hhplus.tdd.domain.coupon.application.command.IssueUserCouponUseCase;
import io.hhplus.tdd.domain.coupon.application.query.GetMyCouponListUseCase;
import io.hhplus.tdd.domain.coupon.presentation.dto.req.CouponIssueReqDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "쿠폰 관리 API", description = "쿠폰 조회 및 발급.")
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Validated
public class CouponController {

    private final GetMyCouponListUseCase getMyCouponListUseCase;
    private final IssueUserCouponUseCase issueUserCouponUseCase;
    private final CouponCacheWarmingUseCase couponCacheWarmingUseCase;

    @PostMapping("/issue")
    public void issueUserCoupopn(@RequestBody @Validated CouponIssueReqDTO couponIssueReqDTO){
        issueUserCouponUseCase.execute(
                new IssueUserCouponUseCase.Input( couponIssueReqDTO.userId() , couponIssueReqDTO.couponId(), couponIssueReqDTO.issuanceId())
        );
    }

    @GetMapping("/{userId}")
    public List<GetMyCouponListUseCase.Output> getUserCouponList(@PathVariable @Positive Long userId){
        return getMyCouponListUseCase.execute(new GetMyCouponListUseCase.Input(userId));
    }

    @GetMapping("/warm")
    public void warm(){
        couponCacheWarmingUseCase.execute();
    }

}
