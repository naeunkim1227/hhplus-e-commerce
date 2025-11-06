package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.coupon.domain.validator.CouponValidator;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import io.hhplus.ecommerce.payment.domain.service.PaymentService;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
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
    public OrderDto excute(OrderCreateFromCartCommand command){
        // 1. 장바구니 아이템 조회
        List<CartItem> cartItems = cartService.getCartItemsByIds(command.getCartItemIds());

        // 2. 재고 검증 및 금액 합산
        BigDecimal totalAmount = BigDecimal.ZERO; //총금액
        BigDecimal discountAmount = BigDecimal.ZERO; //할인금액

        List<Product> products = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        Long orderId = orderService.getNextOrderId();


        for(CartItem cartItem : cartItems) {
            Product product = productService.getProduct(cartItem.getProductId());
            //상품 검증
            productService.validate(product, cartItem.getQuantity());
            products.add(product);

            OrderItem orderItem = orderService.createOrderItem(orderId,cartItem.getProductId(),
                    product.getPrice(),cartItem.getQuantity());

            orderItems.add(orderItem);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        //재고 선점
        for(CartItem cartItem : cartItems){
            productService.reserveStock(orderId, cartItem.getProductId(), cartItem.getQuantity());
        }


        // 3. 쿠폰 검증 및 할인 금액 계산
        if(command.getCouponId() != null) {
            //쿠폰 검증
            couponService.validateCoupon(command.getCouponId(), command.getUserId(), totalAmount);
            discountAmount = couponService.calculateDisCountAmount(command.getCouponId(), totalAmount);
        }

        // 5. 주문 생성
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        Order order = orderService.createOrder(orderId ,command.getUserId(),command.getCouponId()
                , totalAmount, discountAmount, finalAmount);


        // 8. 결제 처리 (비동기)
        paymentService.processPayment(
                order.getId(),
                command.getUserId(),
                finalAmount,
                command.getCartItemIds()
        );

        return OrderDto.from(order, orderItems);
    }


}

