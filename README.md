# 📚 E-Commerce 프로젝트

> HH+ 이커머스 플랫폼 - 도메인 주도 설계 기반 레이어드 아키텍처

---

## 🏗️ 프로젝트 아키텍처

### 레이어드 아키텍처 (Domain-Centric Layered Architecture)

본 프로젝트는 **도메인 로직을 중심**으로 한 레이어드 아키텍처를 채택하여, 비즈니스 규칙의 명확한 분리와 유지보수성을 극대화했습니다.

```
📦 io.hhplus.ecommerce
├── 📂 {domain}                     # 도메인별 모듈 (order, product, cart, coupon, user, payment)
│   ├── 📂 presentation            # 표현 계층
│   │   ├── controller/            # REST API 컨트롤러
│   │   └── dto/                   # API 요청/응답 DTO
│   ├── 📂 application             # 응용 계층
│   │   ├── usecase/               # 비즈니스 유즈케이스 (트랜잭션 경계)
│   │   └── dto/                   # 유즈케이스 Command/Result DTO
│   ├── 📂 domain                  # 도메인 계층 ⭐
│   │   ├── entity/                # 도메인 엔티티 (핵심 비즈니스 로직)
│   │   ├── service/               # 도메인 서비스
│   │   ├── repository/            # 리포지토리 인터페이스
│   │   ├── exception/             # 도메인 예외
│   │   └── event/                 # 도메인 이벤트 (결제 도메인만 적용)
│   └── 📂 infrastructure          # 인프라 계층
│       └── repository/            # 리포지토리 구현체
└── 📂 common                      # 공통 모듈
    ├── config/                    # 설정
    ├── exception/                 # 공통 예외 처리
    └── response/                  # 공통 응답 포맷
```

### 아키텍처 특징 및 장점

#### 1️⃣ **명확한 책임 분리 (Separation of Concerns)**

| 계층 | 역할 | 의존 방향 |
|-----|------|----------|
| **Presentation** | HTTP 요청/응답 처리, API 계약 관리 | → Application |
| **Application** | 유즈케이스 조합, 트랜잭션 경계 관리 | → Domain |
| **Domain** | 핵심 비즈니스 로직, 도메인 규칙 | 독립적 |
| **Infrastructure** | 외부 시스템 연동 (DB, 메시징 등) | → Domain Interface |

**의존성 규칙**: 내부(Domain) → 외부(Infrastructure)로 의존하지 않음 (의존성 역전 원칙)


```java
// 도메인 엔티티 - 순수 비즈니스 로직
@Getter
public class Product {
    private Long stock;

    // 비즈니스 규칙: 재고 감소
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new BusinessException(INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }
}
```



## ⚡ 동시성 제어 구현

### 1. 문제 상황
**쿠폰 선착순 발급 API**에서 동시에 수천 명의 사용자가 쿠폰을 요청하면:
- Race Condition으로 인한 중복 발급
- 재고 수량을 초과한 발급
- DB Lock Contention으로 인한 성능 저하

### 2. 해결 방안: Queue 기반 순차 처리

#### 📌 커스텀 `@QueueAnnotation` 설계

사용자별 큐를 생성하여 **동일 사용자의 요청은 순차 처리**, 다른 사용자 요청은 병렬 처리합니다.

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueueAnnotation {
    String key();        // 큐 식별 키 (userId 등)
    String topic() default "";  // 큐 토픽 (coupon, order 등)
}
```

#### 📌 Queue Manager 구현 
- 이전 TDD과제에서 동시성 구현으로 ReentrantLock을 사용하여 구성하였었는데 실제 동시성 제어는 큐로 하는 경우가 많은 거 같아 큐에 대한 이해도를 높이고자 BlockingQueue를 사용해보았습니다.
- 주문은 아직 적용하지 않았으나, QueueManager 인터페이스를 생성하였고 구현체로 CouponQueueManager,OrderQueueManager를 두었습니다. 각각 큐대상 키가 달라 따로 구현해 보았습니다.

```java
@Component
public class CouponQueueManager implements QueueManager {
    // 사용자별 큐 관리
    private final ConcurrentHashMap<String, BlockingQueue<Runnable>> queue = new ConcurrentHashMap<>();

    @Override
    public void submit(String userId, Runnable task) {
        // 사용자별로 큐 생성 (최대 1개 요청 대기)
        queue.computeIfAbsent(userId, k -> new LinkedBlockingDeque<>(1))
             .add(task);
    }

    @Override
    public void startProcess() {
        // 각 큐마다 별도 스레드에서 순차 처리
        queue.forEach((userId, queue) -> {
            new Thread(() -> {
                while (true) {
                    queue.take().run();  // 큐에서 하나씩 꺼내서 실행
                }
            }).start();
        });
    }
}
```

#### 📌 AOP 사용
- 이전 TDD과제에서 동시성 구현시, 메소드에 결합도가 강한것이 아쉬워서
  aop 커스텀 어노테이션을 만들어 구현해 보았습니다.


```java
@Aspect
@Component
public class QueueAspect {
    private final CouponQueueManager couponQueueManager;

    @Around("@annotation(queueAnnotation)")
    public void around(ProceedingJoinPoint joinPoint, QueueAnnotation queueAnnotation) {
        Runnable task = () -> joinPoint.proceed();

        // 토픽에 따라 해당 큐 매니저에 제출
        switch (queueAnnotation.topic()) {
            case "coupon" -> couponQueueManager.submit(queueAnnotation.key(), task);
            case "order" -> orderQueueManager.submit(queueAnnotation.key(), task);
        }
    }
}
```

### 3. 쿠폰 발급 API 적용 예시

```java
@PostMapping("/coupons/{couponId}/issue")
@QueueAnnotation(topic = "coupon", key = "#userId")  // 어노테이션 적용
public ResponseEntity<CouponIssueResponse> issueCoupon(
    @PathVariable Long couponId,
    @RequestParam Long userId
) {
    return couponIssueUseCase.execute(command);
}
```

---

## 🎯 이벤트 기반 아키텍처 (주문/결제)

### 1. 비동기 결제 처리와 이벤트 발행

주문/결제 프로세스에서 **이벤트 기반 아키텍처**를 적용하여 결제 결과에 따른 후속 처리를 비동기로 분리했습니다.

#### 문제 상황
- 결제 처리는 외부 PG사와 통신이 필요 (느린 I/O)
- 결제 성공/실패에 따른 **후속 처리가 복잡**:
  - 성공 시: 재고 차감, 주문 완료, 장바구니 비우기, 쿠폰 사용 처리
  - 실패 시: 재고 예약 해제, 주문 취소
- 결제와 후속 처리를 **동기로 처리하면 응답 시간 증가**

#### 해결 방안: Spring Events를 활용한 이벤트 발행/구독

```java
@Service
public class PaymentService {
    private final ApplicationEventPublisher eventPublisher;

    public void processPayment(Long orderId, Long userId, BigDecimal amount, List<Long> cartItemIds) {
        try {
            // 1. 외부 PG사 결제 처리 (시뮬레이션)
            Thread.sleep(2000);

            // 2. 잔액 차감
            userService.reduceBalance(userId, amount);

            // 3. 결제 성공 이벤트 발행 ⭐
            PaymentSuccessEvent event = new PaymentSuccessEvent(
                orderId, userId, amount, paymentId, LocalDateTime.now(), cartItemIds
            );
            eventPublisher.publishEvent(event);

        } catch (Exception e) {
            // 4. 결제 실패 이벤트 발행 ⭐
            PaymentFailureEvent event = new PaymentFailureEvent(
                orderId, userId, amount, "결제 실패: " + e.getMessage(), LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);
        }
    }
}
```

### 2. 이벤트 핸들러를 통한 후속 처리

```java
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    @EventListener
    @Async  // 비동기 처리
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        // 1. 재고 차감 (예약 → 확정)
        productService.confirmReservation(event.getOrderId());

        // 2. 주문 상태 변경 (PENDING → COMPLETED)
        orderService.completeOrder(event.getOrderId());

        // 3. 장바구니 비우기
        cartService.clearCartItems(event.getCartItemIds());

        // 4. 외부 데이터 플랫폼 전송 (선택)
        externalService.sendOrderData(event.getOrderId());
    }

    @EventListener
    @Async
    public void handlePaymentFailure(PaymentFailureEvent event) {
        // 1. 재고 예약 해제
        productService.releaseReservation(event.getOrderId());

        // 2. 주문 취소 처리
        orderService.cancelOrder(event.getOrderId());

        // 3. 실패 로그 기록
        log.error("결제 실패 - orderId: {}, reason: {}",
                  event.getOrderId(), event.getFailureReason());
    }
}
```

### 3. 이벤트 기반 아키텍처 장점

| 장점 | 설명 |
|-----|------|
| **느슨한 결합** | PaymentService는 후속 처리 로직을 몰라도 됨 |
| **비동기 처리** | 결제 후 즉시 응답, 후속 처리는 백그라운드 실행 |
| **확장성** | 새로운 후속 처리 추가 시 이벤트 핸들러만 추가 |
| **테스트 용이** | 이벤트 발행 여부만 검증하면 됨 |

### 4. 주문/결제 플로우

```
[주문 생성]
    ↓
[재고 검증 및 예약] ← Optimistic Lock
    ↓
[결제 처리 시작] ← Thread.sleep(외부 PG 시뮬레이션)
    ↓
[결제 성공/실패 이벤트 발행] ⭐
    ↓
┌────────────────┬────────────────┐
│  Success Event  │  Failure Event  │
├────────────────┼────────────────┤
│ - 재고 차감     │ - 재고 예약 해제 │
│ - 주문 완료     │ - 주문 취소      │
│ - 장바구니 삭제 │ - 실패 로그      │
│ - 외부 전송     │                 │
└────────────────┴────────────────┘
```

### 5. 이벤트 테스트 예시

```java
@Test
@DisplayName("결제 성공 시 PaymentSuccessEvent가 발행된다")
void publishesSuccessEvent() {
    // Given
    ArgumentCaptor<PaymentSuccessEvent> eventCaptor =
        ArgumentCaptor.forClass(PaymentSuccessEvent.class);

    // When
    paymentService.processPayment(orderId, userId, amount, cartItemIds);

    // Then: 이벤트 발행 검증
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    PaymentSuccessEvent event = eventCaptor.getValue();
    assertThat(event.getOrderId()).isEqualTo(orderId);
    assertThat(event.getPaymentId()).startsWith("PAY-");
}
```

---

**Last Updated:** 2025-11-07