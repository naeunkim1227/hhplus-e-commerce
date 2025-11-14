package io.hhplus.ecommerce.payment.application.handler;

import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import io.hhplus.ecommerce.payment.domain.event.PaymentFailureEvent;
import io.hhplus.ecommerce.payment.domain.event.PaymentSuccessEvent;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결제 이벤트 핸들러
 * - 결제 성공: 재고 예약 확정 + 재고 차감 + 주문 완료 + 장바구니 비우기
 * - 결제 실패: 재고 예약 만료 + 주문 실패 상태 변경
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final OrderService orderService;
    private final ProductService productService;
    private final CartService cartService;

    /**
     * 결제 성공 이벤트
     * 1. 재고 예약 확정 (CONFIRMED)
     * 2. 실제 재고 차감
     * 3. 주문 완료 상태로 변경
     * 4. 장바구니 비우기
     */
    @Transactional
    @Async
    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("결제 성공 이벤트 처리 시작 - orderId: {}, paymentId: {}",
                event.getOrderId(), event.getPaymentId());

        try {
            Long orderId = event.getOrderId();

            // 1. 주문 정보 조회
            Order order = orderService.getOrder(orderId);
            List<OrderItem> orderItems = orderService.getOrderItems(orderId);

            // 2. 재고 예약 확정
            productService.confirmReservation(orderId);

            // 3. 실제 재고 차감
            for (OrderItem orderItem : orderItems) {
                productService.decreaseStock(orderItem.getProductId(), orderItem.getQuantity());
                log.info("재고 차감 완료 - productId: {}, quantity: {}",
                        orderItem.getProductId(), orderItem.getQuantity());
            }

            // 4. 주문 완료 상태로 변경
            orderService.completeOrder(orderId);
            log.info("주문 완료 처리 - orderId: {}", orderId);

            // 5. 장바구니 비우기
            if (event.getCartItemIds() != null && !event.getCartItemIds().isEmpty()) {
                cartService.clearCartItems(event.getCartItemIds());
                log.info("장바구니 비우기 완료 - orderId: {}, cartItemIds: {}", orderId, event.getCartItemIds());
            }

            log.info("결제 성공 이벤트 처리 완료 - orderId: {}", orderId);

        } catch (Exception e) {
            log.error("결제 성공 이벤트 처리 중 오류 발생 - orderId: {}", event.getOrderId(), e);
            // 실패 시 보상 트랜잭션 처리 필요
        }
    }

    /**
     * 결제 실패 이벤트
     * 1. 재고 예약 해제 (RELEASED)
     * 2. 주문 실패 상태로 변경
     */
    @Transactional
    @Async
    @EventListener
    public void handlePaymentFailure(PaymentFailureEvent event) {
        log.info("결제 실패 이벤트 처리 시작 - orderId: {}, reason: {}",
                event.getOrderId(), event.getFailureReason());

        try {
            Long orderId = event.getOrderId();

            // 1. 재고 예약 해제
            productService.releaseReservation(orderId);
            log.info("재고 예약 해제 완료 - orderId: {}", orderId);

            // 2. 주문 실패 상태로 변경
            orderService.failOrder(orderId);
            log.info("주문 실패 처리 - orderId: {}", orderId);

            log.info("결제 실패 이벤트 처리 완료 - orderId: {}", orderId);

        } catch (Exception e) {
            log.error("결제 실패 이벤트 처리 중 오류 발생 - orderId: {}", event.getOrderId(), e);
        }
    }
}