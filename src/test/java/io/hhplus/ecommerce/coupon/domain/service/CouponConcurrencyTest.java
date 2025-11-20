package io.hhplus.ecommerce.coupon.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.CouponStatus;
import io.hhplus.ecommerce.coupon.domain.entity.CouponType;
import io.hhplus.ecommerce.coupon.domain.exception.CouponErrorCode;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaCouponRepository;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaUserCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 쿠폰 발급 동시성 테스트
 * ExecutorService를 사용해 멀티스레드 환경에서 동시 요청 시뮬레이션
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private JpaCouponRepository jpaCouponRepository;

    @Autowired
    private JpaUserCouponRepository jpaUserCouponRepository;

    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        // 테스트용 쿠폰 생성 (재고 100개)
        Coupon coupon = Coupon.builder()
                .code("TEST")
                .name("동시")
                .totalQuantity(10)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .status(CouponStatus.ACTIVE)
                .type(CouponType.RATE)
                .discountRate(new BigDecimal("0.10"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCoupon = jpaCouponRepository.save(coupon);

        System.out.println("Saved coupon with ID: " + testCoupon.getId());
    }

    @Test
    @DisplayName("동시성 테스트: 10개 쿠폰 20명 요청")
    void issueCoupon_Concurrent() throws InterruptedException {
        // Given
        int threadCount = 20;
        int expectedSuccessCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 1; i <= threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    couponService.issueCoupon(userId, testCoupon.getId());
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getErrorCode() == CouponErrorCode.COUPON_SOLD_OUT) {
                        failCount.incrementAndGet();
                    } else {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            Thread.sleep(200);
        }

        // Then
        assertThat(successCount.get()).isEqualTo(expectedSuccessCount);
        assertThat(failCount.get()).isEqualTo(threadCount - expectedSuccessCount);
    }

    @Test
    @DisplayName("동시성 테스트: 같은 사용자가 동시에 여러 번 요청")
    void issueCoupon_SameUser() throws InterruptedException {
        // Given
        int threadCount = 10;
        long userId = 999L;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger alreadyIssuedCount = new AtomicInteger(0);

        // When: 같은 사용자가 동시에 10번 쿠폰 발급 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    couponService.issueCoupon(userId, testCoupon.getId());
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getErrorCode() == CouponErrorCode.COUPON_ALREADY_ISSUED) {
                        alreadyIssuedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            Thread.sleep(100);
        }

        // Then: 1번만 성공, 나머지 9번은 중복 발급 예외
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(alreadyIssuedCount.get()).isEqualTo(threadCount - 1);

        var userCoupons = jpaUserCouponRepository.findByUserId(userId);
        assertThat(userCoupons).hasSize(1);
    }
}