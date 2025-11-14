package io.hhplus.ecommerce.order.domain.entity;

public enum OrderStatus {
    PENDING,              // 결제 대기
    PAYMENT_COMPLETED,    // 결제 완료
    PREPARING,            // 배송 준비
    SHIPPING,             // 배송 중
    DELIVERED,            // 배송 완료
    CANCELLED,             // 취소됨
    FAILED,
}