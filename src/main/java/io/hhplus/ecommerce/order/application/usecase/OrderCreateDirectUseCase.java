package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateDirectCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.dto.OrderInfo;
import io.hhplus.ecommerce.order.domain.dto.OrderItemInfo;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import io.hhplus.ecommerce.payment.domain.dto.command.PaymentProcessCommand;
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
public class OrderCreateDirectUseCase {
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;
    private final PaymentService paymentService;

    @Transactional
    public OrderDto excute(OrderCreateDirectCommand command) {
        // 1단계: 재고 차감 (실패하면 바로 롤백)
        try {
            productService.decreaseStock(command.getProductId(), command.getQuantity());
        } catch (Exception e) {
            log.warn("재고 차감 실패 - productId: {}, error: {}", command.getProductId(), e.getMessage());
            throw e;  // 복구 불필요, 바로 던짐
        }

        //재고 차감 성공후에 진행
        try {
            Long orderId = orderService.getNextOrderId();

            Product product = productService.getProduct(command.getProductId());
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(command.getQuantity()));

            BigDecimal discountAmount = BigDecimal.ZERO;
            if (command.getCouponId() != null) {
                couponService.validateCoupon(command.getCouponId(), command.getUserId(), totalAmount);
                discountAmount = couponService.calculateDisCountAmount(command.getCouponId(), totalAmount);
            }

            List<OrderItemInfo> items = List.of(OrderItemInfo.from(product, command.getQuantity()));

            Order order = orderService
                    .createOrderWithItems(OrderInfo.from(orderId, command.getUserId(), command.getCouponId(), items, discountAmount));

            PaymentProcessCommand paymentCommand = PaymentProcessCommand.of(
                    orderId,
                    order.getUserId(),
                    order.getFinalAmount(),
                    null
            );

            paymentService.processPayment(paymentCommand);

            return OrderDto.from(order, order.getOrderItems());
        }catch (Exception e){
            //재고 차감 이후 프로세스에서 실패시 보상 트랜잭션 추가
            try {
                productService.increaseStock(command.getProductId(), command.getQuantity());
            } catch (Exception rollbackException) {
                log.error("재고 복구 실패 - productId: {}, quantity: {}, error: {}",
                        command.getProductId(), command.getQuantity(), rollbackException.getMessage());
            }
            throw e;
        }
    }
}

