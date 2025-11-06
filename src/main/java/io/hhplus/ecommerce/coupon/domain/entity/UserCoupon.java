package io.hhplus.ecommerce.coupon.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {
    private Long id;
    private Long userId;
    private Long couponId;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private LocalDateTime expiredAt;


    public boolean isAvailable() {
        return  this.usedAt == null
                && (this.expiredAt == null || LocalDateTime.now().isBefore(this.expiredAt));
    }

    public boolean isExpired() {
        return this.expiredAt != null && LocalDateTime.now().isAfter(this.expiredAt);
    }

    public boolean isUsed() {
        return this.usedAt != null;
    }

}
