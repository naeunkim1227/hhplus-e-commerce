package io.hhplus.ecommerce.coupon.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserCoupon 엔티티 단위 테스트")
class UserCouponTest {


    @Test
    @DisplayName("사용되지 않고 만료되지 않은 쿠폰은 사용 가능하다")
    void available_coupon() {
        // Given
        UserCoupon userCoupon = UserCoupon.builder()
                .id(1L)
                .userId(100L)
                .couponId(200L)
                .issuedAt(LocalDateTime.now().minusDays(30))
                .usedAt(null)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .build();

        assertThat(userCoupon.isAvailable()).isTrue();
        assertThat(userCoupon.isExpired()).isFalse();
        assertThat(userCoupon.isUsed()).isFalse();
    }

    @Test
    @DisplayName("만료된 쿠폰은 사용 불가능하다")
    void expired_coupon() {
        // Given
        UserCoupon userCoupon = UserCoupon.builder()
                .id(1L)
                .userId(100L)
                .couponId(300L)
                .expiredAt(LocalDateTime.now().minusDays(1))
                .build();

        // When & Then
        assertThat(userCoupon.isAvailable()).isFalse();
        assertThat(userCoupon.isExpired()).isTrue();
        assertThat(userCoupon.isUsed()).isFalse();
    }

    @Test
    @DisplayName("이미 사용된 쿠폰은 사용 불가능하다")
    void used_coupon() {
        // Given
        UserCoupon userCoupon = UserCoupon.builder()
                .id(1L)
                .userId(100L)
                .couponId(200L)
                .issuedAt(LocalDateTime.now().minusDays(5))
                .usedAt(LocalDateTime.now().minusDays(1))
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        // When & Then
        assertThat(userCoupon.isAvailable()).isFalse();
        assertThat(userCoupon.isExpired()).isFalse();
        assertThat(userCoupon.isUsed()).isTrue();
    }

}

