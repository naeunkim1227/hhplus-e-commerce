// common/scheduler/LikeSyncScheduler.java
package io.hhplus.ecommerce.common.scheduler;

import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeSyncScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProductRepository productRepository;

    /**
     * 좋아요 동기화 - 6시간마다
     */
    @Scheduled(fixedRate = 21600000)  // 6시간 = 6 * 60 * 60 * 1000
    @Transactional
    public void syncLikeDB() {
        log.info("좋아요 증분 동기화 시작");

        try {
            Set<String> countKeys = redisTemplate.keys("like:count:*");

            if (countKeys == null || countKeys.isEmpty()) {
                return;
            }

            for (String countKey : countKeys) {
                try {
                    Long productId = Long.parseLong(countKey.replace("like:count:", ""));
                    String countStr = redisTemplate.opsForValue().get(countKey);

                    if (countStr == null) continue;
                    int likeCount = Integer.parseInt(countStr);
                    Product product = productRepository.findById(productId)
                            .orElseThrow();

                    product.updateLikeCount(likeCount);
                    productRepository.save(product);
                    log.error("좋아요 동기화 성고오오옹: {}");

                } catch (Exception e) {
                    log.error("좋아요 동기화 실패: {}", e);
                }
            }
        } catch (Exception e) {
            log.error("좋아요 동기화 실패", e);
        }
    }
}