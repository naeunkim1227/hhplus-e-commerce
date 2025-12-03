package io.hhplus.ecommerce.common.config.redis;

import java.time.Duration;

/**
 * RedisTemplate 직접 사용 시 Redis 키 타입 정의
 * CacheType과 동일한 패턴으로 일관성 유지
 */
public enum RedisKeyType {

    // ==================== 랭킹 ====================
    RANKING_POPULAR("ranking:popular", Duration.ofHours(24)),

    RANKING_DAILY("ranking:daily:%s", Duration.ofDays(7)),
    RANKING_WEEKLY("ranking:weekly:%s", Duration.ofDays(30)),
    RANKING_MONTHLY("ranking:monthly:%s", Duration.ofDays(90)),

    // ==================== 상품 ====================
    PRODUCT_LIKE("product:like:%s", Duration.ZERO),
    PRODUCT_VIEW("product:view:%s", Duration.ZERO),
    PRODUCT_ORDER("product:order:%s", Duration.ZERO),
    PRODUCT_CART("product:cart:%s", Duration.ZERO),

    // ==================== 유저 ====================
    USER_VIEWS("user:views:%s", Duration.ofHours(24)),
    USER_LIKES("user:likes:%s", Duration.ZERO),
    USER_ORDERS("user:orders:%s", Duration.ZERO),
    USER_CARTS("user:carts:%s", Duration.ZERO);

    private final String pattern;
    private final Duration ttl;

    RedisKeyType(String pattern, Duration ttl) {
        this.pattern = pattern;
        this.ttl = ttl;
    }

    /**
     * Redis 키 생성
     */
    public String getKey(Object... args) {
        if (args.length == 0) {
            return pattern;
        }
        return String.format(pattern, args);
    }

    /**
     * TTL 반환 (Duration)
     */
    public Duration getTtl() {
        return ttl;
    }

}