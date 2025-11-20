package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateDirectCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.dto.OrderInfo;
import io.hhplus.ecommerce.order.domain.dto.OrderItemInfo;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import io.hhplus.ecommerce.payment.domain.service.PaymentService;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderCreateDirectUseCase {
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;
    private final PaymentService paymentService;


    public OrderDto excute(OrderCreateDirectCommand command) {
        Product product = productService.getProduct(command.getProductId());
        productService.validate(product, command.getQuantity());

        Long orderId = orderService.getNextOrderId();

        productService.decreaseStock(command.getProductId(), command.getQuantity());

        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(command.getQuantity()));

        couponService.validateCoupon(command.getCouponId(), command.getUserId(), totalAmount);
        BigDecimal discountAmount = couponService.calculateDisCountAmount(command.getCouponId(), totalAmount);

        List<OrderItemInfo> items = List.of(OrderItemInfo.from(product, command.getQuantity()));

        Order order = orderService
                .createOrderWithItems(OrderInfo.from(orderId,command.getUserId(),command.getCouponId(),items,discountAmount));

        paymentService.processPayment(
                order.getId(),
                command.getUserId(),
                order.getFinalAmount(),
                null
        );

        return OrderDto.from(order, order.getOrderItems());
    }
}

