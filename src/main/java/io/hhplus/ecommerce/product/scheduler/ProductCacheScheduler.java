package io.hhplus.ecommerce.product.scheduler;

import io.hhplus.ecommerce.common.config.CacheType;
import io.hhplus.ecommerce.product.application.dto.command.ProductPopularCommand;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCacheScheduler {

    private final ProductService productService;

    // 일일 인기상품 캐시 갱신 - 10분마다
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    public void refreshDailyPopularProductsCache() {
        log.info("일일 인기상품 캐시 갱신 시작");
        try {
            productService.getPopularProductsDaily();
        } catch (Exception e) {
            log.error("일일 인기상품 캐시 갱신 실패", e);
        }
    }

    // 주간 인기상품 캐시 갱신 - 1시간마다
    @Scheduled(fixedRate = 3600000) // 1시간 = 3,600,000ms
    public void refreshWeeklyPopularProductsCache() {
        log.info("주간 인기상품 캐시 갱신 시작");
        try {
            productService.getPopularProductsWeekly();
        } catch (Exception e) {
            log.error("주간 인기상품 캐시 갱신 실패", e);
        }
    }

    // 월간 인기상품 캐시 갱신 - 2시간마다
    @Scheduled(fixedRate = 7200000) // 2시간 = 7,200,000ms
    public void refreshMonthlyPopularProductsCache() {
        log.info("월간 인기상품 캐시 갱신 시작");
        try {
            productService.getPopularProductsMonthly();
        } catch (Exception e) {
            log.error("월간 인기상품 캐시 갱신 실패", e);
        }
    }
}

