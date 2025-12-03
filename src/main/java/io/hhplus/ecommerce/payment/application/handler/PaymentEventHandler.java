package io.hhplus.ecommerce.payment.application.handler;

import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import io.hhplus.ecommerce.payment.domain.event.PaymentFailureEvent;
import io.hhplus.ecommerce.payment.domain.event.PaymentSuccessEvent;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import io.hhplus.ecommerce.ranking.domain.service.RankingService;
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
    private final RankingService rankingService;

    /**
     * 결제 성공 이벤트
     * 1. 주문 완료 상태로 변경
     * 2. 장바구니 비우기
     *
     * 참고: 재고는 이미 주문 생성 시 차감됨
     */
    @Transactional
    @Async
    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("결제 성공 이벤트 처리 시작 - orderId: {}, paymentId: {}",
                event.getOrderId(), event.getPaymentId());

        try {
            Long orderId = event.getOrderId();

            // 1. 주문 완료 상태로 변경
            orderService.completeOrder(orderId);
            log.info("주문 완료 처리 - orderId: {}", orderId);

            // 2. 장바구니 비우기
            if (event.getCartItemIds() != null && !event.getCartItemIds().isEmpty()) {
                cartService.clearCartItems(event.getCartItemIds());
                log.info("장바구니 비우기 완료 - orderId: {}, cartItemIds: {}", orderId, event.getCartItemIds());
            }

            //3. 랭킹 메트릭
            rankingService.recordOrder(event.getOrderId());

            log.info("결제 성공 이벤트 처리 완료 - orderId: {}", orderId);

        } catch (Exception e) {
            log.error("결제 성공 이벤트 처리 중 오류 발생 - orderId: {}", event.getOrderId(), e);
        }
    }

    /**
     * 결제 실패 이벤트
     * 1. 재고 복구 (주문 생성 시 차감된 재고를 다시 증가)
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

            // 1. 주문 정보 조회 및 재고 복구
            Order order = orderService.getOrder(orderId);
            List<OrderItem> orderItems = orderService.getOrderItems(orderId);

            for (OrderItem orderItem : orderItems) {
                productService.increaseStock(orderItem.getProductId(), orderItem.getQuantity());
                log.info("재고 복구 완료 - productId: {}, quantity: {}",
                        orderItem.getProductId(), orderItem.getQuantity());
            }

            // 2. 주문 실패 상태로 변경
            orderService.failOrder(orderId);
            log.info("주문 실패 처리 - orderId: {}", orderId);

            log.info("결제 실패 이벤트 처리 완료 - orderId: {}", orderId);

        } catch (Exception e) {
            log.error("결제 실패 이벤트 처리 중 오류 발생 - orderId: {}", event.getOrderId(), e);
        }
    }
}