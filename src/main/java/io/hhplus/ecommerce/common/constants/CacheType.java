package io.hhplus.ecommerce.common.constants;

import java.time.Duration;

public enum CacheType {
    PRODUCTS("products", Duration.ofHours(1)),
    POPULAR_PRODUCTS("popularProducts", Duration.ofMinutes(15));

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
        public static final String POPULAR_PRODUCTS = "popularProducts";
    }
}