package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import io.hhplus.ecommerce.payment.domain.service.PaymentService;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class OrderCreateFromCartUseCase {
    private final OrderService orderService;
    private final ProductService productService;
    private final CartService cartService;
    private final CouponService couponService;
    private final PaymentService paymentService;

    @Transactional
    public OrderDto excute(OrderCreateFromCartCommand command){
        // 1. 장바구니 아이템 조회
        List<CartItem> cartItems = cartService.getCartItemsByIds(command.getCartItemIds());

        Long orderId = orderService.getNextOrderId();

        //2.주문 선점
        List<Product> products = cartItems.stream()
                .map(cartItem -> productService.reserveStock(
                        orderId,
                        cartItem.getProductId(),
                        cartItem.getQuantity()
                ))
                .toList();

        // 3. 쿠폰 검증 (금액 계산 전 사용자 권한 체크)
        if (command.getCouponId() != null) {
            couponService.validateCoupon(command.getCouponId(), command.getUserId(), BigDecimal.ZERO);
        }

        // 4. 주문 생성
       Order order = orderService.createOrderFromCart(command.of(orderId,command.getUserId(), command.getCouponId(),products , cartItems));

        // 5. 결제 처리 (비동기)
        paymentService.processPayment(orderId, order.getUserId(), order.getFinalAmount() , command.getCartItemIds());
        return OrderDto.from(order, order.getOrderItems());
    }

}

