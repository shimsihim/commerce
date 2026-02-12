package io.hhplus.tdd.common.exception;


public class CouponException extends BusinessException {

    public CouponException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CouponException(ErrorCode errorCode , long userId ) {
        super(errorCode , userId);
    }

    public CouponException(ErrorCode errorCode , long userId , long couponId ) {
        super(errorCode , userId  , couponId);
    }
}