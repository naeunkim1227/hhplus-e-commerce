package io.hhplus.ecommerce.coupon.integration;


import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.coupon.application.usecase.CouponIssueUseCase;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@DisplayName("Coupon 통합 테스트 - UseCase + Service + Repository + DB")
public class CouponIntergrationTest {

    @Autowired
    private CouponIssueUseCase couponIssueUseCase;

    @Autowired
    private JpaCouponRepository couponRepository;

    @Autowired
    private JpaUserCouponRepository userCouponRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Coupon testCoupon;

    AtomicInteger successCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        // 자식 엔티티부터 삭제 (외래 키 제약 조건)
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    @DisplayName("쿠폰을 생성하고 조회한다")
    void createAndGetCoupon() {
        testCoupon = Coupon.builder()
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
        Coupon createCoupon =  couponRepository.save(testCoupon);
        couponRepository.flush();

        Optional<Coupon> savedCoupon = couponRepository.findById(createCoupon.getId());

        Assertions.assertAll(
                "쿠폰 생성 후 조회",
                () -> assertThat(savedCoupon).isNotNull(),
                () -> assertThat(savedCoupon.get().getId()).isEqualTo(createCoupon.getId())
        );
    }


    @Test
    @DisplayName("선착순 쿠폰을 발급한다 - 분산락을 사용하여 300명이 동시에 요청했을때 100명만 성공한다.")
    void issueCouponConcurrency() throws InterruptedException {
        // given
        int issuedCoupontCount = 100;

        testCoupon = Coupon.builder()
                .code("TEST")
                .name("동시2")
                .totalQuantity(issuedCoupontCount)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .status(CouponStatus.ACTIVE)
                .type(CouponType.RATE)
                .discountRate(new BigDecimal("0.10"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Long couponId = transactionTemplate.execute(status -> {
            Coupon created = couponRepository.save(testCoupon);
            return created.getId();
        });

        int threadCount = 300;
        ExecutorService executorService = newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when: 100명이 동시에 쿠폰 발급 요청
        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    couponIssueUseCase.execute(userId, couponId);
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        List<UserCoupon> userCoupons = userCouponRepository.findByCouponId(couponId);

        Assertions.assertAll(
                "선착순 쿠폰 검증",
                () -> assertThat(userCoupons).hasSize(issuedCoupontCount),
                () -> assertThat(coupon.getIssuedQuantity()).isEqualTo(issuedCoupontCount)
        );
    }



}
