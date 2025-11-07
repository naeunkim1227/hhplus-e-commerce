package io.hhplus.ecommerce.coupon.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.CouponStatus;
import io.hhplus.ecommerce.coupon.domain.entity.CouponType;
import io.hhplus.ecommerce.coupon.domain.exception.CouponErrorCode;
import io.hhplus.ecommerce.coupon.domain.repository.CouponRepository;
import io.hhplus.ecommerce.coupon.domain.repository.UserCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        // 테스트용 쿠폰 생성 (재고 100개)
        testCoupon = Coupon.builder()
                .id(1L)
                .code("TEST")
                .name("동시")
                .totalQuantity(100)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .status(CouponStatus.ACTIVE)
                .type(CouponType.RATE)
                .discountRate(new BigDecimal("0.10"))
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        couponRepository.save(testCoupon);
    }

    @Test
    @DisplayName("동시성 테스트: 100개 재고에 200명 요청")
    void issueCoupon_Concurrent() throws InterruptedException {
        // Given
        int threadCount = 200;
        int expectedSuccessCount = 100;

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

        // Then: 100개 성공  100개는실패
        assertThat(successCount.get()).isEqualTo(expectedSuccessCount);
        assertThat(failCount.get()).isEqualTo(threadCount - expectedSuccessCount);

        // 실제 발급된 쿠폰 수 확인
        var issuedCoupons = userCouponRepository.findByCouponId(testCoupon.getId());
        assertThat(issuedCoupons).hasSize(expectedSuccessCount);
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
                    latch.await();  // 모든 스레드가 동시에 실행

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


        var userCoupons = userCouponRepository.findByUserId(userId);
        assertThat(userCoupons).hasSize(1);
    }
}