package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.common.kafka.KafkaTopics;
import io.hhplus.ecommerce.common.outbox.DomainType;
import io.hhplus.ecommerce.common.outbox.OutboxService;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateDirectCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.dto.OrderInfo;
import io.hhplus.ecommerce.order.domain.dto.OrderItemInfo;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.event.OrderCompletedEvent;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import io.hhplus.ecommerce.user.domain.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final UserService userService;
    private final OutboxService outboxService;

    @Transactional
    public OrderDto excute(OrderCreateDirectCommand command) {
        // 재고 차감 (실패하면 바로 롤백)
        try {
            productService.decreaseStock(command.getProductId(), command.getQuantity());
        } catch (Exception e) {
            log.warn("재고 차감 실패 - productId: {}, error: {}", command.getProductId(), e.getMessage());
            throw e;  // 복구 불필요, 바로 던짐
        }

        //재고 차감 성공후에 진행
        try {
            //상품 조회
            Product product = productService.getProduct(command.getProductId());
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(command.getQuantity()));

            BigDecimal discountAmount = BigDecimal.ZERO;
            //쿠폰 검증 및 계산
            if (command.getCouponId() != null) {
                couponService.validateCoupon(command.getCouponId(), command.getUserId(), totalAmount);
                discountAmount = couponService.calculateDisCountAmount(command.getCouponId(), totalAmount);
            }
            List<OrderItemInfo> items = List.of(OrderItemInfo.from(product, command.getQuantity()));

            //주문 생성
            Order order = orderService.createOrderWithItems(OrderInfo.from(command.getUserId(), command.getCouponId(), items, discountAmount));

            //잔액 차감
            userService.reduceBalance(command.getUserId(), order.getFinalAmount());

            //주문 완료 처리
            orderService.completeOrder(order.getId());

            //부가로직 이벤트 발행 처리
            OrderCompletedEvent event = new OrderCompletedEvent(
                    order.getId(),
                    order.getUserId(),
                    order.getFinalAmount(),
                    null
            );

            outboxService.save(
                    DomainType.ORDER,
                    KafkaTopics.ORDER_EVENTS,
                    order.getId().toString(),
                    event
            );
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

