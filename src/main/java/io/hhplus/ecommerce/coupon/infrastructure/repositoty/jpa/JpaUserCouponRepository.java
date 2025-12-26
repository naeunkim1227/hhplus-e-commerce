package io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa;

import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaUserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserId(Long userId);

    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

    List<UserCoupon> findByCouponId(Long couponId);

    /**
     * 슬로우 쿼리 시뮬레이션을 위한 메서드
     * 20초 대기 후 사용자의 쿠폰 목록 조회
     */
    @Query(value = "SELECT SLEEP(10) as sleep_result, uc.* FROM user_coupons uc WHERE uc.user_id = :userId", nativeQuery = true)
    List<UserCoupon> findByUserIdWithSlowQuery(@Param("userId") Long userId);
}