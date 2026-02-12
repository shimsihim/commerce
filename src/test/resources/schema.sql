-- 1. 사용자 기본 정보
CREATE TABLE `users`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '사용자 ID (PK)',
    `email`      VARCHAR(100) NOT NULL COMMENT '이메일 (로그인 ID)',
    `password`   VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    `name`       VARCHAR(50)  NOT NULL COMMENT '사용자 실명',
    `phone`      VARCHAR(20)  NOT NULL COMMENT '휴대전화 번호',
    `status`     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 (ACTIVE, DORMANT, WITHDRAWN)',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`),
    UNIQUE KEY `uk_users_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 정보';

-- 2. 사용자 지갑 (현재 잔액 - Hot Table)
-- 사용자 테이블과 1:1 관계이지만, 락 경합 분리를 위해 별도 테이블 사용
CREATE TABLE `user_wallets`
(
    `user_id`    BIGINT         NOT NULL COMMENT '사용자 ID (PK)',
    `balance`    DECIMAL(15, 0) NOT NULL DEFAULT 0 COMMENT '현재 잔액 (원 단위)',
    `version`    BIGINT         NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전 (동시성 제어)',
    `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 지갑 (잔액 관리)';

-- 3. 지갑 이력 (입출금 로그 - Append Only)
CREATE TABLE `user_wallet_histories`
(
    `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '이력 ID',
    `user_id`          BIGINT         NOT NULL COMMENT '사용자 ID',
    `type`             VARCHAR(20)    NOT NULL COMMENT '유형 (CHARGE, USE, REFUND)',
    `amount`           DECIMAL(15, 0) NOT NULL COMMENT '변동 금액 (+/-)',
    `balance_snapshot` DECIMAL(15, 0) NOT NULL COMMENT '변동 후 잔액 스냅샷',
    `reference_id`     BIGINT      DEFAULT NULL COMMENT '관련 주문/이벤트 ID',
    `reference_type`   VARCHAR(50) DEFAULT NULL COMMENT '참조 출처 (ORDER, EVENT)',
    `created_at`       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`id`),
    INDEX              `idx_wallet_hist_user` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='지갑 변동 이력';

-- 4. 상품 기본 정보
CREATE TABLE `products`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '상품 ID',
    `name`           VARCHAR(200)   NOT NULL COMMENT '상품명',
    `price`          DECIMAL(12, 0) NOT NULL COMMENT '판매가',
    `stock_quantity` INT            NOT NULL DEFAULT 0 COMMENT '재고 수량',
    `status`         VARCHAR(20)    NOT NULL DEFAULT 'ON_SALE' COMMENT '판매 상태 (ON_SALE, SOLD_OUT)',
    `version`        BIGINT         NOT NULL DEFAULT 0 COMMENT '재고 차감용 낙관적 락',
    `created_at`     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`id`),
    INDEX            `idx_products_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품 요약 정보';

-- 5. 상품 상세 정보 (Heavy Data)
CREATE TABLE `product_details`
(
    `product_id`       BIGINT NOT NULL COMMENT '상품 ID (PK)',
    `description`      LONGTEXT COMMENT 'HTML 상세 설명',
    `specifications`   JSON COMMENT '상품 스펙 (JSON)',
    `detail_image_url` VARCHAR(2083) COMMENT '상세 이미지 URL',

    PRIMARY KEY (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품 상세 컨텐츠';

-- 6. 쿠폰 정책 (메타 데이터)
CREATE TABLE `coupons`
(
    `id`                    BIGINT         NOT NULL AUTO_INCREMENT COMMENT '쿠폰 ID',
    `code` VARCHAR(50) NOT NULL COMMENT '쿠폰 코드 (예: WELCOME)',
    `name`                  VARCHAR(100)   NOT NULL COMMENT '쿠폰명',
    `type`                  VARCHAR(20)    NOT NULL COMMENT '할인 타입 (FIXED, PERCENTAGE)',
    `discount_value`        DECIMAL(10, 0) NOT NULL COMMENT '할인 값',
    `min_order_amount`      DECIMAL(12, 0) NOT NULL DEFAULT 0 COMMENT '최소 주문 금액',
    `max_discount_amount`   DECIMAL(12, 0)          DEFAULT NULL COMMENT '최대 할인 금액 (정율 시)',
    `total_quantity`        INT                     DEFAULT NULL COMMENT '전체 발행 제한 (NULL=무제한)',
    `issued_quantity`       INT            NOT NULL DEFAULT 0 COMMENT '현재 발행된 수량 (캐싱용)',
    `max_issuance_per_user` INT            NOT NULL DEFAULT 1 COMMENT '1인당 최대 발급 수량',
    `valid_from`            DATETIME       NOT NULL COMMENT '유효기간 시작',
    `valid_until`           DATETIME       NOT NULL COMMENT '유효기간 종료',
    `duration` INT          NOT NULL DEFAULT 1 COMMENT '발급 후 유효기간',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_coupons_code` (`code`),
    INDEX                   `idx_coupons_validity` (`valid_until`, `valid_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='쿠폰 정책 정의';

-- 7. 사용자 보유 쿠폰
CREATE TABLE `user_coupons`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '발급 ID',
    `user_id`     BIGINT      NOT NULL COMMENT '사용자 ID',
    `coupon_id`   BIGINT      NOT NULL COMMENT '쿠폰 ID',
    `valid_until` DATETIME    NOT NULL COMMENT '만료 일시 (Denormalized)',
    `status`      VARCHAR(20) NOT NULL DEFAULT 'UNUSED' COMMENT '상태 (UNUSED, USED, EXPIRED)',
    `version`     BIGINT      NOT NULL DEFAULT 0 COMMENT '사용(Use) 시 낙관적 락 버전',
    `issued_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발급 일시',
    `used_at`     DATETIME             DEFAULT NULL COMMENT '사용 일시',
    `order_id`    BIGINT               DEFAULT NULL COMMENT '사용된 주문 ID',
    `issuance_id` VARCHAR(36)          DEFAULT NULL COMMENT '멱등키 (UUID)',

    PRIMARY KEY (`id`),
    -- [핵심] Kafka 중복 처리 방지 (멱등성 보장)
    UNIQUE KEY `uk_issuance_id` (`issuance_id`),
    INDEX         `idx_user_coupons_lookup` (`user_id`, `status`, `valid_until`),
    INDEX         `idx_user_coupons_coupon` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 쿠폰 보관함';