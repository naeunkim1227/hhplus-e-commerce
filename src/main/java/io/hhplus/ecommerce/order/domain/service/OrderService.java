package io.hhplus.ecommerce.order.domain.service;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.entity.OrderStatus;
import io.hhplus.ecommerce.order.domain.exception.OrderErrorCode;
import io.hhplus.ecommerce.order.domain.repository.OrderItemRepository;
import io.hhplus.ecommerce.order.domain.repository.OrderRepository;
import io.hhplus.ecommerce.product.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public Long getNextOrderId() {
        return orderRepository.nextId();
    }

    /**
     * 주문 생성 (장바구니에서)
     */
    public Order createOrder(
            Long userId,
            Long couponId,
            Long orderId,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        Order order = Order.create(orderId,userId, couponId , totalAmount, discountAmount, finalAmount);
        return orderRepository.save(order);
    }

    public OrderItem createOrderItem(Long orderId, Long productId ,BigDecimal price ,int quantity) {
       OrderItem orderItem = OrderItem.create(orderId,productId,price,quantity);
        return orderItemRepository.save(orderItem);
    }

    /**
     * 주문 조회
     */
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
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
}
