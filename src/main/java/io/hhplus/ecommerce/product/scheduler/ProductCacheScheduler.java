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
    private final CacheManager cacheManager;

    // 인기상품 캐시 갱신 - 10분마다
    @Scheduled(fixedRate = 600000)
    public void refreshPopularProductsCache() {
        log.info("인기상품 캐시 갱신 시작");
        try {
            // 기존 캐시 삭제
            Cache cache = cacheManager.getCache(CacheType.Names.POPULAR_PRODUCTS);
            if (cache != null) {
                cache.clear();
            }

            // 3일 Top 10 갱신
            ProductPopularCommand command3Days = new ProductPopularCommand(3, 10);
            productService.getPopularProducts(command3Days);

            // 7일 Top 10 갱신
            ProductPopularCommand command7Days = new ProductPopularCommand(7, 10);
            productService.getPopularProducts(command7Days);

        } catch (Exception e) {
            log.error("인기상품 캐시 갱신 실패", e);
        }
    }
}

