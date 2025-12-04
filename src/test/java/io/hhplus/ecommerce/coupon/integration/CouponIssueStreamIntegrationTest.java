package io.hhplus.ecommerce.coupon.integration;

import io.hhplus.ecommerce.common.constants.RedisKeyType;
import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.coupon.application.result.CouponIssueDto;
import io.hhplus.ecommerce.coupon.domain.service.CouponIssueQueueService;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.CouponStatus;
import io.hhplus.ecommerce.coupon.domain.entity.CouponType;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaCouponRepository;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaUserCouponRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@DisplayName("Redis Stream 기반 쿠폰 발급 통합 테스트")
public class CouponIssueStreamIntegrationTest {

    @Autowired
    private CouponIssueQueueService couponIssueQueueService;

    @Autowired
    private JpaCouponRepository couponRepository;

    @Autowired
    private JpaUserCouponRepository userCouponRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;


    @BeforeEach
    void setUp() {
        // DB 초기화
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();

        Set<String> keys = redisTemplate.keys("coupon:*");
        if (keys != null && !keys.isEmpty()) {
            keys.stream()
                    .filter(key -> !key.equals("coupon:issue:stream"))  // 스트림은 유지
                    .forEach(redisTemplate::delete);
        }

        try {
            redisTemplate.opsForStream().trim("coupon:issue:stream", 0);
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("쿠폰 발급 요청 시 Redis Stream에 메시지가 추가된다")
    void shouldAddMessageToStreamWhenIssueCoupon() {
        // given
        Long userId = 1L;
        // Redis에 쿠폰 최대 수량 설정
        Long couponId = createTestCoupon(100);
        String maxKey = RedisKeyType.COUPON_ISSUE_MAX.getKey(couponId);
        redisTemplate.opsForValue().set(maxKey, "100");

        // when
        CouponIssueDto result = couponIssueQueueService.addToQueue(userId, couponId);

        // then
        String streamKey = RedisKeyType.COUPON_ISSUE_STREAM.getKey(couponId);
        Long streamSize = redisTemplate.opsForStream().size(streamKey);

        System.out.println("streamKey.." + streamKey);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getWaitNumber()).isEqualTo(1L);
        assertThat(streamSize).isEqualTo(1L);
    }


    @Test
    @DisplayName("대용량 동시성 테스트 - 1000명 요청에 100명만 성공하고 Consumer가 모두 처리한다")
    void concurrenyIssueCoupon_Success() throws InterruptedException {
        // given
        int maxQuantity = 100;
        int threadCount = 1000;
        Long couponId = createTestCoupon(maxQuantity);

        String maxKey = RedisKeyType.COUPON_ISSUE_MAX.getKey(couponId);
        redisTemplate.opsForValue().set(maxKey, String.valueOf(maxQuantity));

        ExecutorService executorService = newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    couponIssueQueueService.addToQueue(userId, couponId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 실패는 무시
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        // Consumer 처리 대기
        Thread.sleep(10000);

        // then
        List<UserCoupon> userCoupons = userCouponRepository.findByCouponId(couponId);
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(successCount.get()).isEqualTo(maxQuantity);
        assertThat(userCoupons).hasSize(maxQuantity);
        assertThat(coupon.getIssuedQuantity()).isEqualTo(maxQuantity);
    }


    @Test
    @DisplayName("Consumer가 메시지를 받아 처리 할 수 있다.")
    void consumerReceiveMessage_Success() throws InterruptedException {
        // given
        Long couponId = createTestCoupon(10);
        Long userId = 1L;

        String maxKey = RedisKeyType.COUPON_ISSUE_MAX.getKey(couponId);
        String streamKey = RedisKeyType.COUPON_ISSUE_STREAM.getKey();

        redisTemplate.opsForValue().set(maxKey, "10");

        // when - 쿠폰 발급 요청
        couponIssueQueueService.addToQueue(userId, couponId);

        // Redis 상태 확인
        Long streamSize = redisTemplate.opsForStream().size(streamKey);

        // Consumer 처리 대기
        for (int i = 1; i <= 5; i++) {
            Thread.sleep(1000);
        }

        List<UserCoupon> userCoupons = userCouponRepository.findByCouponId(couponId);

        // then
        assertThat(streamSize).isEqualTo(1L);
        assertThat(userCoupons).hasSize(1);
    }

    private Long createTestCoupon(int totalQuantity) {
        return transactionTemplate.execute(status -> {
            Coupon coupon = Coupon.builder()
                    .code("TEST_STREAM_" + System.currentTimeMillis())
                    .name("Redis Stream 테스트 쿠폰")
                    .totalQuantity(totalQuantity)
                    .issuedQuantity(0)
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().plusDays(30))
                    .status(CouponStatus.ACTIVE)
                    .type(CouponType.RATE)
                    .discountRate(new BigDecimal("10.00"))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            return couponRepository.save(coupon).getId();
        });
    }
}