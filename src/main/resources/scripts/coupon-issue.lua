-- KEYS[1]: coupon:{couponId} (Hash - 부모)
-- KEYS[2]: coupon:{couponId}:user:{userId} (Set - 자식)
-- ARGV[1]: issuanceId (UUID)

local couponKey = KEYS[1]
local userKey = KEYS[2]
local issuanceId = ARGV[1]

-- 1. [멱등성] 이미 발급받은 이력이 있는지 확인 (Set 조회)
if redis.call('SISMEMBER', userKey, issuanceId) == 1 then
    return {'DUPLICATE'}
end

-- 2. [데이터 조회] 쿠폰 정보 한 번에 가져오기 (HMGET 최적화)
-- 인덱스: 1:total, 2:issued, 3:limit, 4:type, 5:value, 6:minOrder, 7:maxDiscount, 8:duration
local couponInfo = redis.call('HMGET', couponKey,
    'totalQuantity', 'issuedQuantity', 'maxIssuancePerUser',
    'type', 'discountValue', 'minOrderAmount', 'maxDiscountAmount', 'duration')

-- 쿠폰이 존재하지 않음 (Hash 키가 없음)
if not couponInfo[1] then
    return {'NOT_FOUND'}
end

local totalQty = tonumber(couponInfo[1])
local issuedQty = tonumber(couponInfo[2])
local limitPerUser = tonumber(couponInfo[3])

-- 3. [전체 재고] 체크 (-1은 무제한)
if totalQty ~= -1 and issuedQty >= totalQty then
    return {'SOLD_OUT'}
end

-- 4. [개인 한도] 체크 (Set 크기 확인)
local userCount = redis.call('SCARD', userKey)
if userCount >= limitPerUser then
    return {'LIMIT_PER_USER'}
end

-- 5. [발급 실행] (Write)
redis.call('HINCRBY', couponKey, 'issuedQuantity', 1) -- 전체 수량 증가
redis.call('SADD', userKey, issuanceId)               -- 유저 이력 추가

-- 쿠폰의 남은 수명을 유저이력에게 적용
local parentTTL = redis.call('TTL', couponKey)
if parentTTL > 0 then
    redis.call('EXPIRE', userKey, parentTTL)
end

-- [결과 반환] 조회했던 정보 재활용 (DB 재조회 X)
-- couponId는 KEYS[1]에서 파싱하거나 별도 조회 필요하지만, 편의상 Hash에서 읽음
local couponId = redis.call('HGET', couponKey, 'couponId')

return {
    'SUCCESS',
    couponInfo[4], -- type
    couponInfo[5], -- discountValue
    couponInfo[6], -- minOrderAmount
    couponInfo[7], -- maxDiscountAmount
    couponInfo[8], -- duration
    couponId       -- couponId
}