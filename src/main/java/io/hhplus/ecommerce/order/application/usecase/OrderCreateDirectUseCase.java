package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateDirectCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import io.hhplus.ecommerce.payment.domain.service.PaymentService;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderCreateDirectUseCase {
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;
    private final PaymentService paymentService;

    @Transactional
    public OrderDto excute(OrderCreateDirectCommand command) {

        //재고 검증
        Product product  = productService.getProduct(command.getProductId());
        productService.validate(product,command.getQuantity());

        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(command.getQuantity())); //총금액

        //재고 선점
        Long orderId = orderService.getNextOrderId();
        productService.reserveStock(orderId, command.getProductId(), command.getQuantity());

        couponService.validateCoupon(command.getCouponId(), command.getUserId(), totalAmount);
        BigDecimal discountAmount = couponService.calculateDisCountAmount(command.getCouponId(), totalAmount);

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        Order order = orderService.createOrder(orderId ,command.getUserId(),command.getCouponId()
                , totalAmount, discountAmount, finalAmount);

        OrderItem orderItem = orderService.createOrderItem(command.getProductId(),
                product.getPrice(), command.getQuantity());

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);

        paymentService.processPayment(
                order.getId(),
                command.getUserId(),
                finalAmount,
                null
        );

        return OrderDto.from(order, orderItems);
    }
}

