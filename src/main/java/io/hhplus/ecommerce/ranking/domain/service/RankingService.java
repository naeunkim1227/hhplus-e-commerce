package io.hhplus.ecommerce.ranking.domain.service;

import io.hhplus.ecommerce.common.constants.RedisKeyType;
import io.hhplus.ecommerce.ranking.policy.RankingPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;

    public void recordView(Long productId, Long userId) {
        String productViewKey = RedisKeyType.PRODUCT_VIEW.getKey(productId);
        String userViewKey = RedisKeyType.USER_VIEWS.getKey(userId);

        Long added = redisTemplate.opsForSet().add(userViewKey, productId.toString());

        if (added == null || added == 0) {
            return;
        }

        redisTemplate.opsForValue().increment(productViewKey);
        redisTemplate.expire(userViewKey, RedisKeyType.USER_VIEWS.getTtl());
        incrementMetric(productId, RankingPolicy.VIEW_INCREMENT_SCORE);
    }

    public void recordLike(Long productId) {
        incrementMetric(productId, RankingPolicy.LIKE_INCREMENT_SCORE);
    }

    public void removeLike(Long productId) {
        incrementMetric(productId, -RankingPolicy.LIKE_INCREMENT_SCORE);
    }

    public void recordOrder(Long productId) {
        String productOrderKey = RedisKeyType.PRODUCT_ORDER.getKey(productId);

        redisTemplate.opsForValue().increment(productOrderKey);
        incrementMetric(productId, RankingPolicy.ORDER_INCREMENT_SCORE);
    }

    public void recordCart(Long productId, Long userId) {
        String productCartKey = RedisKeyType.PRODUCT_CART.getKey(productId);
        String userCartKey = RedisKeyType.USER_CARTS.getKey(userId);

        Long added = redisTemplate.opsForSet().add(userCartKey, productId.toString());
        if (added == null || added == 0) {
            return;
        }

        redisTemplate.opsForValue().increment(productCartKey);
        incrementMetric(productId, RankingPolicy.CART_INCREMENT_SCORE);
    }

    private void incrementMetric(Long productId, double incrementScore) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        redisTemplate.opsForZSet().incrementScore(
                RedisKeyType.RANKING_POPULAR.getKey(today),
                String.valueOf(productId),
                incrementScore
        );

        redisTemplate.expire(
                RedisKeyType.RANKING_POPULAR.getKey(today),
                RedisKeyType.RANKING_POPULAR.getTtl()
        );

    }
}
