package io.hhplus.ecommerce.coupon.domain.service;

import io.hhplus.ecommerce.common.constants.RedisKeyType;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.application.result.CouponIssueDto;
import io.hhplus.ecommerce.coupon.domain.exception.CouponErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 쿠폰 발급 요청을 Redis Stream 큐에 추가하는 서비스
 * Redis 기반 동시성 제어 및 재고 관리를 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssueQueueService {

    private final RedisTemplate<String, String> redisTemplate;

    public CouponIssueDto addToQueue(Long userId, Long couponId) {
        String countKey = RedisKeyType.COUPON_ISSUE_COUNT.getKey(couponId);
        String streamKey = RedisKeyType.COUPON_ISSUE_STREAM.getKey();
        String maxKey = RedisKeyType.COUPON_ISSUE_MAX.getKey(couponId);
        String setKey = RedisKeyType.COUPON_ISSUE_SET.getKey(couponId);
        long time = System.currentTimeMillis();

        // 중복 발급 체크
        Long addedCount = redisTemplate.opsForSet().add(setKey, userId.toString());

        if(addedCount == 0){
            throw new BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 재고 증가 및 확인
        Long count = redisTemplate.opsForValue().increment(countKey);
        String maxCountStr = redisTemplate.opsForValue().get(maxKey);
        Long maxCount = maxCountStr != null ? Long.parseLong(maxCountStr) : 0L;

        if(count > maxCount){
            // 재고 초과 시 롤백
            redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.opsForSet().remove(setKey, userId.toString());
            throw new BusinessException(CouponErrorCode.COUPON_SOLD_OUT);
        }

        // Redis Stream에 발급 요청 추가
        try {
            Map<String, String> message = Map.of(
                    "couponId", couponId.toString(),
                    "userId", userId.toString(),
                    "timestamp",String.valueOf(time)
            );
            redisTemplate.opsForStream().add(streamKey, message);
        } catch (Exception e) {
            // 스트림 추가 실패 시 롤백
            redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.opsForSet().remove(setKey, userId.toString());
            throw new RuntimeException("Stream 추가 실패", e);
        }

        // 대기번호 발급
        return CouponIssueDto.from(couponId, userId, count);
    }
}