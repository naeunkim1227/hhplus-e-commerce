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
import java.util.ArrayList;
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
    public OrderDto excute(OrderCreateFromCartCommand command) {
        // 1. 장바구니 아이템 조회
        List<CartItem> cartItems = cartService.getCartItemsByIds(command.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new BusinessException(OrderErrorCode.CART_NOT_FOUND);
        }

        //락 획득 순서 일관성 보장 처리 추가
        List<CartItem> sortedCartItems = cartItems.stream()
                .sorted((a, b) -> a.getProductId().compareTo(b.getProductId()))
                .toList();

        List<CartItem> decreasedItems = new ArrayList<>();

        try {
            Long orderId = orderService.getNextOrderId();

            List<Long> productIds = sortedCartItems.stream()
                    .map(CartItem::getProductId)
                    .toList();

            List<Product> products = productService.getProductsByIds(productIds);

            // 재고 차감 (성공한 것만 추적)
            for (CartItem cartItem : sortedCartItems) {
                productService.decreaseStock(cartItem.getProductId(), cartItem.getQuantity());
                decreasedItems.add(cartItem);
            }

            // OrderItemInfo변환
            List<OrderItemInfo> items = sortedCartItems.stream()
                    .map(cart -> {
                        Product product = products.stream()
                                .filter(p -> p.getId().equals(cart.getProductId()))
                                .findFirst()
                                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
                        return new OrderItemInfo(product.getId(), product.getPrice(), cart.getQuantity());
                    })
                    .toList();

            BigDecimal totalAmount = items.stream()
                    .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discountAmount = BigDecimal.ZERO;
            if (command.getCouponId() != null) {
                couponService.validateCoupon(command.getCouponId(), command.getUserId(), totalAmount);
                discountAmount = couponService.calculateDisCountAmount(command.getCouponId(), totalAmount);
            }

            Order order = orderService
                    .createOrderWithItems(OrderInfo.from(orderId, command.getUserId(), command.getCouponId(), items, discountAmount));

            paymentService.processPayment(orderId, order.getUserId(), order.getFinalAmount(), command.getCartItemIds());
            return OrderDto.from(order, order.getOrderItems());

        } catch (Exception e) {
            // 보상 트랜잭션: 성공한 재고 차감만 복구
            for (CartItem cartItem : decreasedItems) {
                try {
                    productService.increaseStock(cartItem.getProductId(), cartItem.getQuantity());
                } catch (Exception rollbackException) {
                    log.error("재고 복구 실패 - productId: {}, quantity: {}, error: {}",
                            cartItem.getProductId(), cartItem.getQuantity(), rollbackException.getMessage());
                }
            }
            throw e;
        }
    }

}

