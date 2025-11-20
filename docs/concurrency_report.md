# 동시성 제어 보고서

## 목차

1. [개요](#1-개요)
2. [선착순 쿠폰 동시성 제어 (비관적 락)](#2-선착순-쿠폰-동시성-제어-비관적-락)
   - 2.1 동시성 문제 식별
   - 2.2 낙관적 락 시도 및 한계
   - 2.3 비관적 락 선택 및 구현
   - 2.4 JUnit 동시성 테스트
   - 2.5 JMeter 부하 테스트
   - 2.6 결과 분석
3. [포인트 차감 동시성 제어 (낙관적 락)](#3-포인트-차감-동시성-제어-낙관적-락)
   - 3.1 동시성 문제 식별
   - 3.2 낙관적 락 선택 이유
   - 3.3 구현 코드
   - 3.4 테스트 코드 및 결과
4. [재고 차감 동시성 제어 (조건부 UPDATE)](#4-재고-차감-동시성-제어-조건부-update)
   - 4.1 동시성 문제 식별
   - 4.2 조건부 UPDATE 선택 이유
   - 4.3 구현 코드
   - 4.4 아키텍처 변경
5. [JMeter 테스트 범위 및 선정 기준](#5-jmeter-테스트-범위-및-선정-기준)
6. [전체 결론](#6-전체-결론)

---

## 1. 개요

본 프로젝트에서는 세 가지 주요 동시성 제어 대상을 식별하고, 각각의 특성에 맞는 최적의 동시성 제어 방식을 적용했습니다.

### 동시성 제어 대상 및 전략 정리

| 기능 | 동시성 제어 방식 | 선택 이유 |
|------|-----------------|----------|
| **선착순 쿠폰** | 비관적 락 (FOR UPDATE) | 데이터 정합성 최우선, 재고 초과 발급 절대 불가 |
| **포인트 차감** | 낙관적 락 (@Version) | 경합 빈도 낮음, 재시도로 사용자 경험 보장 |
| **재고 차감** | 조건부 UPDATE | 높은 동시성 + 빠른 응답, Atomic operation |

### 테스트 방법론

- **JUnit 테스트**: 모든 기능의 동시성 제어 로직 검증
- **JMeter 부하 테스트**: 선착순 쿠폰만 실제 프로덕션 환경 시뮬레이션
  - 이유: 가장 높은 동시성이 예상되며, HTTP/네트워크 오버헤드 측정 필요

---

## 2. 선착순 쿠폰 동시성 제어 (낙관적 > 비관적 락)

### 2.1 동시성 문제 식별

선착순 쿠폰 발급 시스템에서는 다음과 같은 동시성 문제가 발생할 수 있습니다.

#### 발급 프로세스

```java
// CouponService.java
@Transactional
public UserCoupon issueCoupon(Long userId, Long couponId) {
    // 1. 중복 발급 체크
    userCouponRepository.findByUserIdAndCouponId(userId, couponId)
            .ifPresent(c -> {
                throw new BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED);
            });

    // 2. 쿠폰 조회
    Coupon coupon = couponRepository.findByIdForUpdate(couponId)
            .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));

    // 3. 발급 가능 여부 검증
    coupon.isAvailableIssue();

    // 4. 발급 수량 증가
    coupon.increaseIssuedQuantity();

    // 5. 사용자 쿠폰 저장
    UserCoupon userCoupon = UserCoupon.create(userId, couponId);
    return userCouponRepository.save(userCoupon);
}
```

#### Race Condition 시나리오

```
재고 1개 남은 쿠폰에 2명의 사용자가 동시 요청

[Thread A]                    [Thread B]
쿠폰 조회 (재고: 1)
                              쿠폰 조회 (재고: 1)
발급량 체크 통과 ✅
                              발급량 체크 통과 ✅
발급량 증가 (1 → 2)
                              발급량 증가 (2 → 3)
발급 성공                     발급 성공

결과: 재고 1개인데 2명에게 발급 ❌
```

**발생 원인**:
- `coupon.isAvailableIssue()` (검증)와 `coupon.increaseIssuedQuantity()` (증가) 사이에 시간차 발생
- JPA 메커니즘으로 인해 트랜잭션 커밋 시점에 UPDATE 실행
- 두 스레드가 동일한 쿠폰 엔티티를 읽고 각각 수정하여 저장

### 2.2 낙관적 락 시도 및 한계

#### 낙관적 락 구현

처음에는 JPA의 `@Version` 어노테이션으로 낙관적 락을 시도했습니다.

```java
@Entity
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version  // 낙관적 락
    private Long version;

    private Integer totalQuantity;
    private Integer issuedQuantity;
}
```

#### 동작 원리

```
1. SELECT * FROM coupons WHERE id = 1
   → version = 10, issuedQuantity = 99

2. 비즈니스 로직 수행 (메모리에서)
   → issuedQuantity++ (100)

3. UPDATE coupons
   SET issuedQuantity = 100, version = 11
   WHERE id = 1 AND version = 10  ← 버전 체크

만약 다른 트랜잭션이 먼저 version을 11로 변경했다면:
→ UPDATE 실패 (WHERE 조건 불일치)
→ ObjectOptimisticLockingFailureException 발생
```

#### 테스트 결과 및 문제점

**테스트 조건**: 쿠폰 10개 재고, 재시도 최대 5회 (300ms 간격)

| 동시 스레드 수 | 결과 | 비고 |
|-------------|------|------|
| 200 스레드 | ✅ 성공 | 10개 정확히 발급 |
| 300 스레드 | ❌ 실패 | 일부 요청이 5회 재시도 후에도 실패 |

**선착순 쿠폰에 부적합한 이유**:

시나리오: 10개 재고, 1,000명 동시 요청

낙관적 락 사용 시:
→ 1,000개 트랜잭션이 모두 version = 1인 쿠폰 조회
→ 첫 번째만 성공, 나머지 999개는 예외 발생
→ 재시도 → 또 충돌 → 계속 재시도
→ 1,000 + 999 + 998 + ... + 991 = 총 9,955번 시도

문제점:
- 성능 저하 및 시스템 부하
- DB 커넥션 과다 요청
- 응답 시간 증가

-> 동시 요청수가 많은 api의 경우 낙관적 락은 전체적인 시스템의 장애를 유발할 수도 있다는 결론에 다다랐습니다.


### 2.3 비관적 락으로 변경

#### 선택 이유

**선착순 쿠폰 특성**:
- 100개 재고에 1,000명 요청 시 900명은 어차피 실패
- 낙관적 락: 900명이 재시도 → 시스템 부하
- 비관적 락: 900명이 대기 후 실패 → 안정적

-> 낙관적 락에서 발생할 수 있는, Race Condition 문제 재시도 로직으로 인한 
요청수 증가등의 문제를 해결 할 수 있다고 판단하여 변경 하였습니다.

#### 구현 코드

**Repository**:
```java
// JpaCouponRepository.java
public interface JpaCouponRepository extends JpaRepository<Coupon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.id = :id")
    Optional<Coupon> findByIdForUpdate(@Param("id") Long id);
}
```

**동작 흐름**:
```
[Transaction A]                      [Transaction B]
BEGIN
SELECT ... FOR UPDATE (락 획득 ✅)
                                     BEGIN
                                     SELECT ... FOR UPDATE (대기 ⏳)
UPDATE issuedQuantity = 101
COMMIT (락 해제 🔓)
                                     (락 획득 ✅)
                                     UPDATE issuedQuantity = 102
                                     COMMIT
```

**Service**:
```java
// CouponService.java
@Transactional(readonly=false)
public UserCoupon issueCoupon(Long userId, Long couponId) {
    // 1. 중복 발급 체크
    userCouponRepository.findByUserIdAndCouponId(userId, couponId)
            .ifPresent(c -> {
                throw new BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED);
            });

    // 2. 비관적 락으로 쿠폰 조회 (FOR UPDATE)
    Coupon coupon = couponRepository.findByIdForUpdate(couponId)
            .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));

    // 3. 발급 가능 여부 검증
    coupon.isAvailableIssue();

    // 4. 발급 수량 증가 (락 보호 하에 안전하게 수행)
    coupon.increaseIssuedQuantity();

    // 5. 사용자 쿠폰 저장
    UserCoupon userCoupon = UserCoupon.create(userId, couponId);
    return userCouponRepository.save(userCoupon);
}  // ← 트랜잭션 종료 시 락 자동 해제
```

### 2.4 JUnit 동시성 테스트

#### 테스트 코드

```java
// CouponIntegrationTest.java
@Test
@DisplayName("선착순 쿠폰을 발급한다 - 1000명이 동시에 요청했을때 100명만 성공한다.")
void issueCouponConcurrency() throws InterruptedException {
    // Given
    int issuedCouponCount = 100;

    testCoupon = Coupon.builder()
            .totalQuantity(issuedCouponCount)
            .issuedQuantity(0)
            .build();

    Long couponId = transactionTemplate.execute(status -> {
        Coupon created = couponRepository.save(testCoupon);
        return created.getId();
    });

    int threadCount = 1000;  // 1000명 동시 요청
    ExecutorService executorService = newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    // When
    for (int i = 0; i < threadCount; i++) {
        long userId = i + 1;
        executorService.submit(() -> {
            retryIssueCoupon(userId, couponId, false, 0);
            latch.countDown();
        });
    }

    latch.await();
    executorService.shutdown();

    // Then
    Coupon result = couponRepository.findById(couponId).orElseThrow();
    assertThat(result.getIssuedQuantity()).isEqualTo(issuedCouponCount);
    assertThat(successCount.get()).isEqualTo(issuedCouponCount);
}
```

#### 테스트 결과

- ✅ **테스트 성공**: 1,000개 스레드 동시 실행
- ✅ **정확한 쿠폰 발급**: 100개 쿠폰만 정확히 발급
- ✅ **동시성 보장**: 발급 수량 초과 없음 (Race Condition 방지)
- ✅ **Lost Update 방지**: DB 상태와 카운터가 정확히 일치
- ⏱️ **총 실행 시간**: 약 5-10초 (비관적 락으로 인한 순차 처리)

### 2.5 JMeter 부하 테스트

#### JMeter 테스트 범위 및 선정 기준
본 프로젝트에서는 **선착순 쿠폰 발급**만 JMeter로 부하 테스트를 진행했습니다.

#### 선착순 쿠폰을 선택한 이유

1. **가장 높은 동시성 예상**:
   - 100개 쿠폰에 1,000명 동시 요청 가능
   - 이벤트성 쿠폰의 경우 순간적인 트래픽 폭증

2. **실제 프로덕션 환경 시뮬레이션 필요**:
   - JUnit 테스트 성공이 실제 성능을 보장하지 않음
   - HTTP 네트워크 오버헤드, 커넥션 풀, 락 경합 등 확인 필요


#### 테스트 환경 설정

**최적화 설정**: Hikari Connection Pool 증설과 Tomcat 스레드 풀 조정

```properties
# Tomcat Thread Pool (HTTP 요청 처리)
server.tomcat.threads.max=500
server.tomcat.threads.min-spare=50
server.tomcat.max-connections=1000
server.tomcat.accept-count=500
server.tomcat.connection-timeout=30000

# Hikari Connection Pool (DB 커넥션 관리)
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=3000
spring.datasource.hikari.validation-timeout=2000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

**설정 변경 이유**:
- 설정 적용 전: 1초당 100개 요청만 성공
- **Tomcat max threads 500**: 동시에 최대 500개의 HTTP 요청 처리 가능
- **Hikari max pool size 50**: DB 커넥션을 50개까지 생성하여 동시성 향상
- 설정 후: 200 req/s까지 안정적 처리 가능

#### 테스트 결과

- 1초당 100스레드 > ✅ 성공
![1초당 100스레드](./image/1s_100_suc.png)

- 1초당 200스레드 > Hikari/Tomcat 설정 후 ✅ 성공
![1초당 200스레드](./image/1s_200_suc.png)

- 1초당 400스레드 > ❌ 실패
![1초당 400스레드](./image/1s_400_fail.png)

- 3초당 500스레드 > ✅ 성공
![3초당 500스레드](./image/3s_500_suc.png)

- 6초당 1000스레드 > ✅ 성공
![6초당 1000스레드](./image/6s_1000_suc.png)

**성능 한계**:
- ✅ **초당 200 req/s**: 안정적 처리 (성공률 100%)
- ⚠️ **초당 300-500 req/s**: 부분 실패 발생 가능
- ❌ **초당 1,000 req/s**: 대량 실패 (성공률 15%)

#### 초당 200개 요청 (Ramp-up 1초) - ✅ 성공
- ✅ **성공률**: 100% (200/200)
- ⏱️ **평균 응답시간**: ~120ms
- 📊 **처리량**: ~200 req/s
- 📌 **결론**: Hikari/Tomcat 설정 후 200 req/s까지 안정적 처리 확인

#### 초당 1,000개 요청 (Ramp-up 1초) - ❌ 실패
- ❌ **성공률**: 15% (150/1,000)
- ❌ **실패 건수**: 850건
- ⏱️ **평균 응답시간**: ~1,000ms
- 🔴 **에러 타입**: HTTP 500 Internal Server Error
- 📊 **처리량**: ~150 req/s
- 📌 **실패 원인**: 커넥션 풀 고갈, 락 경합, 타임아웃

### 2.6 결과 분석

#### JUnit vs JMeter 성능 차이

| 구분 | JUnit | JMeter |
|------|-------|--------|
| 1,000 동시 요청 | ✅ 성공 | ❌ 실패 (15% 성공) |
| 테스트 환경 | In-Process | HTTP 네트워크 |
| 재시도 로직 | 있음 (최대 5회) | 없음 |
| 대기 시간 | 최소화 (메모리 공유) | 네트워크 지연 포함 |

#### 실패 원인 분석

**1. 네트워크 오버헤드**:
- JUnit: 애플리케이션 내부 메서드 호출로 네트워크 지연 없음
- JMeter: HTTP 요청/응답 과정에서 네트워크 지연 발생

**2. 커넥션 풀 제약**:
```
Hikari Maximum Pool Size: 50
동시 요청: 1,000개
→ 950개 요청은 커넥션 대기 또는 타임아웃
```

**3. 락 경합(Lock Contention)**:
- 비관적 락 특성: 순차 처리
- 첫 번째 요청이 락을 획득하면 나머지 999개는 대기
- DB 커넥션을 점유한 채로 락 해제 대기
- 커넥션 풀 고갈로 인한 신규 요청 실패

**4. 트랜잭션 처리 시간**:
```
쿠폰 발급 처리 시간: ~50-150ms
1,000개 순차 처리 시간: 50초 ~ 150초
→ 대부분 요청이 3초 타임아웃 초과
```

```
- 초당 200 req/s까지 안정적 처리 확인
- Race Condition 문제 해결 
- 순차적 요청 보장
- 그러나 제이미터로 테스트 시에 과도한 요청은 락경합을 일으켜 실패
```

---

## 3. 포인트 차감 동시성 제어 (낙관적 락)

### 3.1 동시성 문제 식별

사용자 잔액(balance) 차감 시 Lost Update 문제가 발생할 수 있습니다.

#### Lost Update 시나리오

```
잔액: 10,000원

[결제 A: 5,000원]              [환불 A: 3,000원]
getUser() → 10,000
                                getUser() → 10,000
reduceBalance(5,000)
→ balance = 5,000
                                increseBalance(3,000)
                                → balance = 13,000
save() → 5,000 저장
                                save() → 13,000 저장 (덮어쓰게 됨)

결과: 사용자 잔액 정합성이 깨짐
```

### 3.2 낙관적 락 선택 이유

#### 포인트 차감 특성 분석

- **경합 빈도 낮음**: 동일 사용자의 포인트는 해당 사용자만 사용
- **동시 요청 드묾**: 사용자가 동시에 여러 결제를 진행하는 경우는 드묾, 그러나 발생할 가능성이 있음
- **배치/환불 시나리오**: 일부 동시성 상황이 있지만 빈도가 낮음

#### 낙관적 락을 선택한  이유
- **경합이 드물어 충돌률 낮음** → 재시도 Retry 어노테이션을 사용
- 포인트 경합이 드물어 순차 처리(락 대기)가 불필요
- 선착순 쿠폰처럼 높은 동시성 환경이 아님
- 사용자별로 독립적이므로 락 경합이 거의 없음


### 3.3 구현 코드

#### User 엔티티에 @Version 추가
#### UserService에 재시도 로직 추가

```java
// UserService.java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * 잔액 차감 (낙관적 락 + 재시도)
     */
    @Retryable(
        value = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void reduceBalance(Long userId, BigDecimal amount) {
        User user = getUser(userId);
        user.reduceBalance(amount);
        userRepository.save(user);
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
```


### 3.4 테스트 코드 및 결과

#### 동시성 테스트 코드

```java
// UserIntegrationTest.java
@Test
@DisplayName("잔액 차감 동시성 테스트 - 100개 스레드가 동시에 100원씩 차감")
void decreaseBalanceConcurrency() throws InterruptedException {
    // Given
    User user = createUser("박물개", 10000);
    User savedUser = jpaUserRepository.save(user);
    jpaUserRepository.flush();
    Long userId = savedUser.getId();

    int threadCount = 5;  
    int decreaseAmount = 1000;  // 각 스레드마다 100원씩 차감

    ExecutorService executorService = newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    // When
    for (int i = 0; i < threadCount; i++) {
        executorService.submit(() -> {
            try {
                userService.reduceBalance(userId, BigDecimal.valueOf(decreaseAmount));
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
    }

    latch.await();
    executorService.shutdown();

    // Then
    User resultUser = jpaUserRepository.findById(userId).orElseThrow();
    assertThat(resultUser.getBalance()).isEqualByComparingTo(String.valueOf(userBalance - (decreaseAmount * threadCount)));

}
```

#### 테스트 결과

**JUnit 동시성 테스트 (5 스레드)**:
- ✅ **자동 재시도**: OptimisticLockException 발생 시 자동 재시도로 모두 성공
- ⏱️ **실행 시간**: 약 2-3초 (재시도 포함)

```
- 포인트 차감은 경합이 드물어 **낙관적 락이 효과적**
- 재시도 로직으로 **사용자 경험 보장**
- 비관적 락 대비 **성능 우수**
```

---

## 4. 재고 차감 동시성 제어 (조건부 UPDATE)

### 4.1 동시성 문제 식별

상품 재고 차감 시 Race Condition이 발생할 수 있습니다.

#### Race Condition 시나리오

```
재고: 10개

[주문 A: 10개]                [주문 B: 10개]
getProduct() → stock=10
                               getProduct() → stock=10
validate() ✅
                               validate() ✅
decreaseStock(10)
→ stock = 0
                               decreaseStock(10)
                               → stock = -10 (초과 판매!)

결과: 재고 10개인데 20개 판매됨 ❌
```

### 4.2 조건부 UPDATE 선택 이유

#### 조건부 UPDATE가 적합하다고 생각한 이유

1.  **재시도 불필요**: 성공/실패가 즉시 결정됨
3. **성능 우수**: 락 대기 없음
4. **동시성 제어 간단**: DB 레벨에서 처리

-> 선착순 쿠폰에 비해 동시 요청수가 많지 않을 수도 있으나, 꾸준히
테이블 접근이 일어날 수 있다고 생각하여 조건부 업데이트로 설정 하였습니다.
또한 재고 차감의 경우 주문 프로세스에서 일어나는 로직이기 때문에 낙관적 락으로 선택하였을 경우
실패시 롤백하는 과정 또한 불필요한 자원을 소모할 수 도 있다는 생각이 들었습니다.
비관적 락은 테이블 접근에 대한 대기시간을 점진적으로 증가시키기 때문에 적절치 않다고 생각했습니다.

### 4.3 구현 코드

#### Repository에 조건부 UPDATE 추가

```java
// JpaProductRepository.java
public interface JpaProductRepository extends JpaRepository<Product, Long> {
    /**
     * 재고 차감 (조건부 UPDATE)
     * stock >= quantity 조건으로 동시성 제어
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity " +
           "WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStock(@Param("productId") Long productId,
                      @Param("quantity") int quantity);

    /**
     * 재고 증가 (결제 실패 시 복구용)
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity " +
           "WHERE p.id = :productId")
    int increaseStock(@Param("productId") Long productId,
                      @Param("quantity") int quantity);
}
```

#### ProductService 구현

```java
// ProductService.java
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * 재고 차감 (조건부 UPDATE로 동시성 제어)
     */
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        int updated = productRepository.decreaseStock(productId, quantity);
        if (updated == 0) {
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
    }

    /**
     * 재고 증가 (결제 실패 시 복구용)
     */
    @Transactional
    public void increaseStock(Long productId, int quantity) {
        productRepository.increaseStock(productId, quantity);
    }
}
```

#### 주문 생성 시 재고 차감

```java
// OrderCreateDirectUseCase.java
@Service
public class OrderCreateDirectUseCase {
    public OrderDto execute(OrderCreateDirectCommand command) {
        Product product = productService.getProduct(command.getProductId());
        productService.validate(product, command.getQuantity());

        Long orderId = orderService.getNextOrderId();

        // 조건부 UPDATE로 재고 차감 (동시성 제어)
        productService.decreaseStock(command.getProductId(), command.getQuantity());

        // ... 주문 생성 및 결제 처리
        BigDecimal totalAmount = product.getPrice()
            .multiply(BigDecimal.valueOf(command.getQuantity()));

        couponService.validateCoupon(command.getCouponId(), command.getUserId(), totalAmount);
        BigDecimal discountAmount = couponService
            .calculateDisCountAmount(command.getCouponId(), totalAmount);

        List<OrderItemInfo> items = List.of(OrderItemInfo.from(product, command.getQuantity()));
        Order order = orderService.createOrderWithItems(
            OrderInfo.from(orderId, command.getUserId(), command.getCouponId(), items, discountAmount)
        );

        paymentService.processPayment(order.getId(), command.getUserId(),
            order.getFinalAmount(), null);

        return OrderDto.from(order, order.getOrderItems());
    }
}
```

**동작 과정**:
1. WHERE 조건 확인: `stock >= quantity`
2. 조건 만족 시: UPDATE 실행, **1 반환**
3. 조건 불만족 시: UPDATE 안 함, **0 반환**
4. 0 반환 시: `INSUFFICIENT_STOCK` 예외 발생

**동시성 제어 원리**:
```
재고: 10개

[주문 A: 10개]                           [주문 B: 10개]
UPDATE ... WHERE stock >= 10
→ 성공 (stock = 0) ✅
                                         UPDATE ... WHERE stock >= 10
                                         → 실패 (stock = 0 < 10)
                                         → 0 반환 → 예외 발생 ✅

결과: A만 성공, B는 재고 부족으로 실패
```

### 4.4 아키텍처 변경

#### ProductReservation 제거

**이전 구조** (ProductReservation 사용):
```
주문 생성 → ProductReservation 생성 (예약)
결제 성공 → confirmReservation() + decreaseStock() (실제 차감)
결제 실패 → releaseReservation() (예약 해제)
```

**현재 구조** (조건부 UPDATE):
```
주문 생성 → decreaseStock() (즉시 차감)
결제 성공 → 아무 작업 없음 (이미 차감됨)
결제 실패 → increaseStock() (재고 복구)
```

---

## 5. 전체 결론

### 5.1 동시성 제어 전략 요약

| 기능 | 동시성 제어 방식 | 성능 | 정합성 | 적용 이유 |
|------|-----------------|------|--------|----------|
| **선착순 쿠폰** | 비관적 락 (FOR UPDATE) | 200 req/s | 100% | 데이터 정합성 최우선 |
| **포인트 차감** | 낙관적 락 (@Version) | 우수 | 100% | 경합 드묾, 재시도 효과적 |
| **재고 차감** | 조건부 UPDATE | 우수 | 100% | Atomic, 아키텍처 단순 |

### 5.2 주요 학습 내용

#### 1. 낙관적 락 vs 비관적 락

**낙관적 락**:
- 경합이 드문 경우 효과적
- 재시도 로직 필수
- 높은 동시성에서는 부적합

**비관적 락**:
- 데이터 정합성이 중요한 경우 선택
- 순차 처리로 성능 저하
- 커넥션 풀 설정 중요

#### 2. 조건부 UPDATE의 효과

- Atomic operation으로 재시도 불필요
- 아키텍처 단순화 효과
- DB 레벨 동시성 제어로 성능 우수

#### 3. JUnit vs JMeter

**JUnit 테스트**:
- 동시성 제어 로직 검증
- In-Process로 빠른 실행
- 네트워크 오버헤드 없음

**JMeter 테스트**:
- 실제 프로덕션 환경 성능 측정
- HTTP/네트워크 오버헤드 확인
- 시스템 병목점 파악

#### 4. 시스템 한계 인식

- 비관적 락: **초당 200 req/s 한계**
- 커넥션 풀, 스레드 풀 설정의 중요성
- JUnit 성공이 프로덕션 성공을 보장하지는 않는다.
- 향후 **Redis 분산락** 등 추가 개선 방안 고려

### 5.3 최종 정리

- **포인트 차감**: 경합이 드물고 동시성 충돌이 많지 않을 것으로 판단하여 낙관적 락으로 설정
- **재고 차감**: 선착순 쿠폰 처럼 단시간에 매우 많은 요청이 올 것 같지는 않으나, 가장 많은 조회가 일어나는 테이블 이라고 생각하여 조건부 UPDATE로 설정하였습니다.
- **선착순 쿠폰**: 낙관적 락의 경우 높은 동시성 환경에서 재시도 횟수가 급증하여 성능 저하 및 시스템 부하가 발생할 수 있어,  비관적 락으로 변경하였습니다.
  이를 통해 Lace Condition,Lost Update,중복 발급,데이터 정합성 문제를 해결하였습니다.
  그러나 제이미터로 테스틑를 진행하였을때는 커넥션 점유시간 문제로 인하여  Tomcat 스레드 풀 증가(max 500) 및 Hikari Connection Pool 확장(max 50) 조정하였음에도 1초당 200개까지만 동시성을 보장하였으며 그 이상은 계속해서 실패하였습니다.  
  단위 테스트 성공이 실제 프로덕션 환경 성능을 보장하지 않고, 비관적 락이 무조건적인 해결 방안은 아니라는 것을 테스트를 통해 알게되었습니다.
  비관적 락은 데이터 정합성을 보장하지만 동시성 처리 능력 제한이 있기 때문에 이를 보장할 수 있는 방안(ex.레디스 분산락)에 대해 고민하게 되었습니다.
  비관적 락이 동시성 문제의 완벽한 해결책은 아니며, 시스템 요구사항과 예상 트래픽에 따라 적절한 아키텍처 수준의 개선이 필요함을 확인했습니다. 

  
### 6. 느낀 점
- 이번 과제를 통해 운이 좋게도(?) 종류별로 락을 사용할 수 있었습니다. 비관적,낙관적,조건부 업데이트에 대하여 어떤 상황에 쓰는 것이 좋을지 헷갈리는 부분이 많았는데,멘토링을 받고 직접 테스트 코드를 작성하면서 적절한 락에 대해 고민할 수 있던 계기가 되었습니다.



