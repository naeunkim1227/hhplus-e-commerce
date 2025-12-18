package io.hhplus.ecommerce.common.outbox;


public enum OutboxStatus {
    PENDING,    // 발행 대기
    PUBLISHED,  // 발행 완료
    FAILED      // 발행 실패 (재시도 횟수 초과)
}