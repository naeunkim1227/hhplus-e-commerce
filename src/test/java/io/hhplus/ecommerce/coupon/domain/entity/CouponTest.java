package io.hhplus.ecommerce.coupon.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Coupon 엔티티 단위 테스트")
class CouponTest {

    @Test
    @DisplayName("쿠폰을 생성한다")
    void create_Coupon_Success() {
        // Given When
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("COUPON1")
                .name("쿠폰1")
                .totalQuantity(1000)
                .issuedQuantity(0)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .status(CouponStatus.ACTIVE)
                .type(CouponType.RATE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Then
        assertThat(coupon).isNotNull();
        assertThat(coupon.getId()).isEqualTo(1L);
        assertThat(coupon.getTotalQuantity()).isEqualTo(1000);
        assertThat(coupon.getIssuedQuantity()).isEqualTo(0);
        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE);
        assertThat(coupon.getType()).isEqualTo(CouponType.RATE);
    }
}