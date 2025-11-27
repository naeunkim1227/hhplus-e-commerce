package io.hhplus.ecommerce;

import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.CouponStatus;
import io.hhplus.ecommerce.coupon.domain.entity.CouponType;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaCouponRepository;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaUserCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
@DisplayName("분산락 Redis 키 등록 확인 테스트")
public class DistributedLockRedisTest {

    @Autowired
    private JpaCouponRepository couponRepository;

    @Autowired
    private JpaUserCouponRepository userCouponRepository;

    @Autowired
    private RedissonClient redissonClient;

    @BeforeEach
    void setUp() {
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    @DisplayName("락생성 테스트 - 30초 유지")
    void testRedisLockDirectly() throws InterruptedException {
        String testLockKey = "test:lock:1";
        var lock = redissonClient.getLock(testLockKey);

        boolean acquired = lock.tryLock(1, 30, TimeUnit.SECONDS);
        if (acquired) {
            try {
                RKeys keys = redissonClient.getKeys();

                Iterable<String> allKeys = keys.getKeys();
                boolean found = false;
                for (String key : allKeys) {
                    if (key.contains(testLockKey) || key.contains("lock")) {
                        found = true;
                    }
                }

                Thread.sleep(30000);
                assertThat(found).as("락 키가 Redis에 존재해야 함").isTrue();
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("락 획득 실패");
        }
    }

}