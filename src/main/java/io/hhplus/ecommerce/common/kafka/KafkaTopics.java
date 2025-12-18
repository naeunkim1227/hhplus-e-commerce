package io.hhplus.ecommerce.common.kafka;

public class KafkaTopics {

    // 주문 도메인 이벤트
    public static final String ORDER_EVENTS = "order-events";

    // 상품 도메인 이벤트
    public static final String PRODUCT_EVENTS = "product-events";

    // 쿠폰 도메인 이벤트
    public static final String COUPON_EVENTS = "coupon-events";

    // 사용자 도메인 이벤트
    public static final String USER_EVENTS = "user-events";

    // 결제 도메인 이벤트
    public static final String PAYMENT_EVENTS = "payment-events";

    private KafkaTopics() {
        // 유틸리티 클래스
    }
}