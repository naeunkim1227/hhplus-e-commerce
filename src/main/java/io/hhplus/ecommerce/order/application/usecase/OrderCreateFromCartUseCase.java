package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.dto.OrderInfo;
import io.hhplus.ecommerce.order.domain.dto.OrderItemInfo;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.exception.OrderErrorCode;
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

        if(cartItems.isEmpty()){
            throw new BusinessException(OrderErrorCode.CART_NOT_FOUND);
        }

        // 2. 재고 선점
        Long orderId = orderService.getNextOrderId();
        List<Product> products = cartItems.stream()
                .map(cartItem -> productService.reserveStock(
                        orderId,
                        cartItem.getProductId(),
                        cartItem.getQuantity()
                ))
                .toList();

        // OrderItemInfo변환
        List<OrderItemInfo> items = cartItems.stream()
                .map(cart -> {
                    Product product = products.stream()
                            .filter(p -> p.getId().equals(cart.getProductId()))
                            .findFirst()
                            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
                    return new OrderItemInfo(product.getId(), product.getPrice(), cart.getQuantity());
                })
                .toList();

        // 4. 총액 계산 및 쿠폰 할인
        BigDecimal totalAmount = items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (command.getCouponId() != null) {
            couponService.validateCoupon(command.getCouponId(), command.getUserId(), totalAmount);
            discountAmount = couponService.calculateDisCountAmount(command.getCouponId(), totalAmount);
        }

        // 5. 주문 생성 (공통 메서드 사용)
        Order order = orderService
                .createOrderWithItems(OrderInfo.from(orderId,command.getUserId(),command.getCouponId(),items,discountAmount));

        // 6. 결제 처리 (비동기)
        paymentService.processPayment(orderId, order.getUserId(), order.getFinalAmount(), command.getCartItemIds());

        return OrderDto.from(order, order.getOrderItems());
    }

}

