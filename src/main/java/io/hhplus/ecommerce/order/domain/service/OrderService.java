package io.hhplus.ecommerce.order.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.order.domain.dto.OrderInfo;
import io.hhplus.ecommerce.order.domain.dto.OrderItemInfo;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.entity.OrderStatus;
import io.hhplus.ecommerce.order.domain.exception.OrderErrorCode;
import io.hhplus.ecommerce.order.domain.repository.OrderItemRepository;
import io.hhplus.ecommerce.order.domain.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponService couponService;

    /**
     * 주문 금액 계산 결과
     */
    @Getter
    @AllArgsConstructor
    public static class OrderCalculation {
        private final BigDecimal totalAmount;
        private final BigDecimal discountAmount;
        private final BigDecimal finalAmount;
        private final List<OrderItem> orderItems;
    }



    public Long getNextOrderId() {
        return orderRepository.nextId();
    }

    /**
     * 주문만 생성 (OrderItem 없이)
     * @deprecated createOrderWithItems 사용을 권장합니다
     */
    @Deprecated
    public Order createOrder(
            Long orderId,
            Long userId,
            Long couponId,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        Order order = Order.create(orderId, userId, couponId, totalAmount, discountAmount, finalAmount);
        return orderRepository.save(order);
    }

    public OrderItem createOrderItem(Long productId, BigDecimal price, int quantity) {
        return OrderItem.create(productId, price, quantity);
    }

    /**
     * 주문 조회
     */
    public Order getOrder(Long orderId) {
        return orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * 주문 완료 처리
     */
    public void completeOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.changeStatus(OrderStatus.PAYMENT_COMPLETED);
        orderRepository.save(order);
    }

    /**
     * 주문 실패 처리
     */
    public void failOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.changeStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    /**
     * 주문 금액 계산 (도메인 중립적)
     */
    public OrderCalculation calculateOrder(OrderInfo orderInfo) {
        // 1. 총 금액 계산 및 OrderItem 생성
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemInfo item : orderInfo.orderItems()) {
            OrderItem orderItem = createOrderItem(
                    item.productId(),
                    item.price(),
                    item.quantity()
            );
            orderItems.add(orderItem);

            BigDecimal itemTotal = item.price()
                    .multiply(BigDecimal.valueOf(item.quantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 2. 최종 금액
        BigDecimal finalAmount = totalAmount.subtract(orderInfo.discountAmount());

        return new OrderCalculation(totalAmount, orderInfo.discountAmount(), finalAmount, orderItems);
    }


    /**
     * 주문 생성 공통화
     */
    public Order createOrderWithItems(OrderInfo orderInfo) {
        // 1. 주문 계산
        OrderCalculation calc = calculateOrder(orderInfo);

        // 2. Order 생성
        Order order = Order.create(
                orderInfo.orderId(), orderInfo.userId(), orderInfo.couponId() != null ? orderInfo.couponId() : 0L,
                calc.getTotalAmount(), calc.getDiscountAmount(), calc.getFinalAmount()
        );

        // 3. OrderItem 추가 (영속 엔티티에 추가)
        calc.getOrderItems().forEach(order::addOrderItem);

        // 4. 저장 (cascade로 orderItems도 함께 저장)
        return orderRepository.save(order);
    }
}
