# DB 최적화 보고서 

## 1. 개요

이 문서는 인기 상품 조회 쿼리의 실행 계획을 인덱스 최적화 전후로 비교 분석하였습니다.

### 분석 쿼리

```sql
EXPLAIN ANALYZE SELECT p.*
FROM products p
INNER JOIN order_items oi ON p.id = oi.product_id
INNER JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'PAYMENT_COMPLETED'
  AND o.ordered_at >= '2024-12-01 00:00:00'
GROUP BY p.id
ORDER BY SUM(oi.quantity) DESC
LIMIT 5;
```

### 테스트 환경
- MySQL 8.0
- 테스트 데이터
  - products: 50개
  - orders: 2,000개 (PAYMENT_COMPLETED: 202개)
  - order_items: 5,286개
- 테스트 기간: 2024-12-01 이후 주문 (14건)

---

## 2. 인덱스 설정 적 쿼리 

### EXPLAIN ANALYZE 결과

[쿼리실행내역](https://github.com/user-attachments/assets/02fb5d94-c09d-4c8c-a5b2-c7a6d5ef0341)

```
-> Limit: 5 row(s)  (actual time=7.07..7.08 rows=5 loops=1)
    -> Sort: `sum(oi.quantity)` DESC, limit input to 5 row(s) per chunk  (actual time=7.07..7.07 rows=5 loops=1)
        -> Table scan on <temporary>  (actual time=7.05..7.06 rows=27 loops=1)
            -> Aggregate using temporary table  (actual time=7.05..7.05 rows=27 loops=1)
                -> Nested loop inner join  (cost=4210 rows=263) (actual time=0.207..6.99 rows=38 loops=1)
                    -> Nested loop inner join  (cost=2371 rows=5256) (actual time=0.069..4.46 rows=5286 loops=1)
                        -> Table scan on oi  (cost=531 rows=5256) (actual time=0.06..1.01 rows=5286 loops=1)
                        -> Single-row index lookup on p using PRIMARY (id=oi.product_id)  (cost=0.25 rows=1) (actual time=525e-6..546e-6 rows=1 loops=5286)
                    -> Filter: ((o.`status` = 'PAYMENT_COMPLETED') and (o.ordered_at >= TIMESTAMP'2024-12-01 00:00:00'))  (cost=0.25 rows=0.05) (actual time=410e-6..410e-6 rows=0.00719 loops=5286)
                        -> Single-row index lookup on o using PRIMARY (id=oi.order_id)  (cost=0.25 rows=1) (actual time=243e-6..265e-6 rows=1 loops=5286)
```


#### 1. 실행 시간
- **총 실행 시간**: 7.07ms
- **스캔된 행 수**: 5,286개 (order_items 전체)
- **예상 비용**: 4,210
- **최종 결과**: 5개 상품

---

## 2. 인덱스 최적화 전략

### 추가된 인덱스

```sql
ALTER TABLE orders ADD INDEX idx_orders_ordered_at_status (ordered_at, status);
ALTER TABLE order_items ADD INDEX idx_order_items_order_id (order_id);
ALTER TABLE order_items ADD INDEX idx_order_items_product_id (product_id);
```

[인덱스추가](https://github.com/user-attachments/assets/462d8edb-aa7a-4c13-9819-a575cadb684d)


#### 1. ordered_at, status
- **사유**: WHERE 절의 두 조건이기 때문에 복합 인덱스로 지정하였으며, 카디널리티가 더 넓은 ordered_at을 우선 순위로 지정했습니다.

#### 2. order_id
- Nested Loop Join을 위해 order_items,orders의 연결 컬럼을 인덱스로 지정했습니다.

#### 3. product_id
- order_items와 products 간 조인 최적화를 위해 인덱스로 지정했습니다.



## 4. 인덱스 최적화 후 실행 계획

### EXPLAIN ANALYZE 결과
[인덱스추가후실행계획](https://github.com/user-attachments/assets/d3f7ff3d-c845-4e9f-8983-2556611c6762)
```
| -> Limit: 5 row(s)  (actual time=0.716..0.716 rows=5 loops=1)
    -> Sort: `sum(oi.quantity)` DESC, limit input to 5 row(s) per chunk  (actual time=0.715..0.716 rows=5 loops=1)
        -> Table scan on <temporary>  (actual time=0.691..0.694 rows=27 loops=1)
            -> Aggregate using temporary table  (actual time=0.69..0.69 rows=27 loops=1)
                -> Nested loop inner join  (cost=75.1 rows=49.1) (actual time=0.122..0.567 rows=38 loops=1)
                    -> Nested loop inner join  (cost=57.9 rows=49.1) (actual time=0.0969..0.408 rows=38 loops=1)
                        -> Filter: ((o.`status` = 'PAYMENT_COMPLETED') and (o.ordered_at >= TIMESTAMP'2024-12-01 00:00:00'))  (cost=40.7 rows=18.7) (actual time=0.0327..0.0813 rows=14 loops=1)
                            -> Covering index range scan on o using idx_orders_ordered_at_status over ('2024-12-01 00:00:00' <= ordered_at AND 'PAYMENT_COMPLETED' <= status)  (cost=40.7 rows=187) (actual time=0.0247..0.0561 rows=187 loops=1)
                        -> Index lookup on oi using idx_order_items_order_id (order_id=o.id)  (cost=0.671 rows=2.63) (actual time=0.0218..0.023 rows=2.71 loops=14)
                    -> Single-row index lookup on p using PRIMARY (id=oi.product_id)  (cost=0.252 rows=1) (actual time=0.00396..0.00398 rows=1 loops=38)
```


#### 5. 개선 후 실행 시간
- **총 실행 시간**: 0.331ms
- **초기 스캔 행 수**: 187개 → 필터링 후 14개 (기존: 5,286개)
- **예상 비용**: 75.1 (**98.2% 감소**, 기존: 4,210)
- **최종 결과**: 5개 상품 (동일)

---

## 5. 성능 비교 요약

| 지표 | 최적화 전 | 최적화 후 | 개선율 |
|-----|---------|---------|--------|
| 실행 시간 | 7.07ms | 0.331ms | **21.4배 향상** |
| 예상 비용 | 4,210 | 75.1 | **98.2% 감소** |
| 초기 스캔 행 수 | 5,286 | 187 | **96.5% 감소** |
| 필터링 후 조인 대상 | 5,286 | 14 | **99.7% 감소** |
| 조인 순서 | oi → p → o | o → oi → p | **최적화** |
| 인덱스 활용 | PRIMARY only | Covering Index + Lookup Index | **효율적** |

---
