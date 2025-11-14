package io.hhplus.ecommerce.coupon.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long couponId;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private LocalDateTime expiredAt;

    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
    }

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

    public static UserCoupon create(Long userId, Long couponId) {
        return UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .build();
    }





}
