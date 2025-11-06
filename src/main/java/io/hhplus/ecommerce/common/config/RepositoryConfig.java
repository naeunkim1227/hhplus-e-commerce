package io.hhplus.ecommerce.common.config;

import io.hhplus.ecommerce.cart.domain.repository.CartRepository;
import io.hhplus.ecommerce.cart.infrastructure.InMemoryCartRepository;
import io.hhplus.ecommerce.coupon.domain.repository.CouponRepository;
import io.hhplus.ecommerce.coupon.domain.repository.UserCouponRepository;
import io.hhplus.ecommerce.coupon.infrastructure.InMemoryCouponRepository;
import io.hhplus.ecommerce.coupon.infrastructure.InMemoryUserCouponRepository;
import io.hhplus.ecommerce.order.domain.repository.OrderItemRepository;
import io.hhplus.ecommerce.order.domain.repository.OrderRepository;
import io.hhplus.ecommerce.order.infrastructure.InMemoryOrderItemRepository;
import io.hhplus.ecommerce.order.infrastructure.InMemoryOrderRepository;
import io.hhplus.ecommerce.product.domain.repository.ProductRepository;
import io.hhplus.ecommerce.product.domain.repository.ProductReservationRepository;
import io.hhplus.ecommerce.product.infrastructure.InMemoryProductRepository;
import io.hhplus.ecommerce.product.infrastructure.InMemoryProductReservationRepository;
import io.hhplus.ecommerce.user.infrastructure.repositoty.UserRepository;
import io.hhplus.ecommerce.user.infrastructure.InMemoryUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public ProductRepository productRepository() {
        return new InMemoryProductRepository();
        // return new JpaProductRepository();
    }

    @Bean
    public ProductReservationRepository productReservationRepository() {
        return new InMemoryProductReservationRepository();
        // return new JpaProductReservationRepository();
    }

    @Bean
    public CartRepository cartRepository() {
        return new InMemoryCartRepository();
        // return new JpaCartRepository();
    }

    @Bean
    public CouponRepository couponRepository() {
        return new InMemoryCouponRepository();
        // return new JpaCouponRepository();
    }

    @Bean
    public OrderRepository orderRepository() {
        return new InMemoryOrderRepository();
        // return new JpaOrderRepository();
    }

    @Bean
    public OrderItemRepository orderItemRepository() {
        return new InMemoryOrderItemRepository();
        // return new JpaOrderItemRepository();
    }

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
        // return new JpaUserRepository();
    }

    @Bean
    public UserCouponRepository userCouponRepository() {
        return new InMemoryUserCouponRepository();
    }

}