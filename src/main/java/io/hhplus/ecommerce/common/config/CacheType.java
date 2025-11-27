package io.hhplus.ecommerce.common.config;

import java.time.Duration;

public enum CacheType {
    PRODUCTS("products", Duration.ofHours(1)),
    COUPONS("coupons", Duration.ofMinutes(5)),
    POPULAR_PRODUCTS("popularProducts", Duration.ofMinutes(15)),
    USERS("users", Duration.ofMinutes(30)),
    ORDERS("orders", Duration.ofMinutes(10));

    private final String cacheName;
    private final Duration ttl;

    CacheType(String cacheName, Duration ttl) {
        this.cacheName = cacheName;
        this.ttl = ttl;
    }

    public String getCacheName() { return cacheName; }
    public Duration getTtl() { return ttl; }

    public static class Names {
        public static final String PRODUCTS = "products";
        public static final String COUPONS = "coupons";
        public static final String POPULAR_PRODUCTS = "popularProducts";
        public static final String USERS = "users";
        public static final String ORDERS = "orders";
    }
}