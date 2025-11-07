package io.hhplus.ecommerce.coupon.domain.service;

import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.repository.CouponRepository;
import io.hhplus.ecommerce.coupon.domain.repository.UserCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService Test")
public class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("유저의 쿠폰을 조회할 수 있다.")
    void getUsercoupon_Success(){
        //given
        Long userId = 1L;
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .issuedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(10))
                .couponId(1L)
                .build();

        when(userCouponRepository.findByUserId(userId))
                .thenReturn(List.of(userCoupon));

        //when
        List<UserCoupon> userCouponList = couponService.getUserCoupon(userId);

        //then
        assertThat(userCouponList).isNotNull();
        assertThat(userCouponList).hasSize(1);
        assertThat(userCouponList.get(0).getUserId()).isEqualTo(userId);
    }


}
