package io.hhplus.ecommerce.order.domain.service;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.order.domain.dto.OrderInfo;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.entity.OrderStatus;
import io.hhplus.ecommerce.order.domain.exception.OrderErrorCode;
import io.hhplus.ecommerce.order.domain.repository.OrderItemRepository;
import io.hhplus.ecommerce.order.domain.repository.OrderRepository;
import io.hhplus.ecommerce.product.domain.entity.Product;
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

    /**
     * 주문 아이템 요청 (도메인 중립적)
     */
    public record OrderItemRequest(Long productId, BigDecimal price, int quantity) {}

    public Long getNextOrderId() {
        return orderRepository.nextId();
    }

    /**
     * 주문 생성 (장바구니에서)
     */
    public Order createOrderFromCart(OrderInfo orderInfo) {
        long orderId = this.getNextOrderId();

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Product product : orderInfo.getProducts()) {
            CartItem cartItem = orderInfo.getCartItems().stream()
                    .filter(c -> c.getProductId().equals(product.getId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

            BigDecimal itemTotal = BigDecimal.valueOf(cartItem.getQuantity())
                    .multiply(product.getPrice());
            totalAmount = totalAmount.add(itemTotal);

            // orderItem 저장
            OrderItem orderItem = createOrderItem(
                    product.getId(),
                    product.getPrice(),
                    cartItem.getQuantity()
            );
            orderItems.add(orderItem);
        }

        if (orderInfo.getCouponId() != null) {
            //쿠폰 검증
            couponService.validateCoupon(orderInfo.getCouponId(), orderInfo.getUserId(), totalAmount);
            discountAmount = couponService.calculateDisCountAmount(orderInfo.getCouponId(), totalAmount);
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // order 저장
        Order order = createOrder(orderId, orderInfo.getUserId(), orderInfo.getCouponId(), totalAmount, discountAmount, finalAmount);
        orderItems.forEach(order::addOrderItem);
        orderRepository.save(order);
        return order;
    }

    /**
     * 주문 생성
     */
    public Order createOrderDirect(OrderInfo orderInfo) {
        long orderId = this.getNextOrderId();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal discountAmount = couponService.calculateDisCountAmount(orderInfo.getCouponId(), totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // order 저장
        Order order = createOrder(orderId, orderInfo.getUserId(), orderInfo.getCouponId(), totalAmount, discountAmount, finalAmount);
        orderRepository.save(order);
        return order;
    }

    /**
     * 주문 생성 (장바구니에서)
     */
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
        OrderItem orderItem = OrderItem.create(productId, price, quantity);
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

    /**
     * 주문 금액 계산 (도메인 중립적)
     * @param orderId 주문 ID
     * @param items 주문 아이템 요청 목록
     * @param discountAmount 할인 금액 (외부에서 계산됨)
     */
    public OrderCalculation calculateOrder(
            Long orderId,
            List<OrderItemRequest> items,
            BigDecimal discountAmount
    ) {
        // 1. 총 금액 계산 및 OrderItem 생성
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest item : items) {
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
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        return new OrderCalculation(totalAmount, discountAmount, finalAmount, orderItems);
    }
}
