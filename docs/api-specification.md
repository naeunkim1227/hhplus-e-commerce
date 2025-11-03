# 4. 🔌 API 명세서

## 목차
1. [공통 사항](#공통-사항)
2. [상품 API](#1-상품-api)
3. [장바구니 API](#2-장바구니-api)
4. [주문 API](#3-주문-api)
5. [쿠폰 API](#4-쿠폰-api)
6. [잔액 API](#5-잔액-api)
7. [에러 응답](#에러-응답)

---

## 공통 사항

### 공통 응답 형식
다음과 같은 공통 응답 형식으로 리턴합니다.

### 성공
```json
{
  "status" : 200,
  "data": {},
  "error" :null,
  "timestamp": "2025-10-28T12:00:00Z"
}


```
### 실패
```json
{
  "status": 400,
  "data": null,
  "error": {
    "code": "STOCK_INSUFFICIENT",
    "message": "재고가 부족합니다",
    "details": {
      "requestedQuantity": 10,
      "availableQuantity": 5
    }
  },
  "timestamp": "2025-10-28T12:00:00Z"
}
```

### 에러 코드별 공통 사용 응답 예시

| HTTP Status | error.code              | message      | 사용 케이스 |
|-------------|-------------------------|--------------|-------------|
| 400         | `INVALID_REQUEST`       | 잘못된 요청입니다    | 필수 파라미터 누락 |
| 400         | `VALIDATION_FAILED`     | 입력값이 올바르지 않습니다 | Bean Validation 실패 |
| 401         | `UNAUTHORIZED`          | 인증이 필요합니다    | 로그인 안 함 |
| 403         | `FORBIDDEN`             | 권한이 없습니다     | 접근 권한 없음 |
| 409         | `ALREADY_EXISTS`        | 이미 존재하는 데이터입니다 | 중복 생성 시도 |
| 500         | `INTERNAL_SERVER_ERROR` | 서버 오류가 발생했습니다 | 예상치 못한 오류 |
| 429         | `RATE_LIMIT_EXCEEDED`   | 요청 횟수 제한 초과  | 


---

## 1. 상품 API

### 1.1 상품 목록 조회

**Endpoint:** `GET /products`

**설명:** 상품 목록을 조회합니다.

**Parameters:**

| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|---------|------|------|------|--------|
| page | integer | N | 페이지 번호 | 1 |
| size | integer | N | 페이지 크기 (최대 100) |


**성공 응답:** `200 OK`
```json
{
  "status" : 200,
  "data": {
    "products": [
      {
        "id": 1,
        "name": "최고심인형",
        "price": 19000,
        "createdAt": "2025-10-20T10:00:00Z",
        "status" : "ACTIVE",
        "stock" : 100
      },
      {
        "id": 1,
        "name": "미피인형",
        "price": 29000,
        "createdAt": "2025-10-20T10:00:00Z",
        "status" : "OUT_OF_STOCK",
        "stock" : 0
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 150,
      "totalPages": 8
    }
  },
  "error": null,
  "timestamp": "2025-10-28T12:00:00Z"
}
```

#### Response 필드 설명

| 필드 | 타입 | 설명                                              |
|-----|------|-------------------------------------------------|
| products | array | 상품 목록                                           |
| products[].id | integer | 상품 ID                                           |
| products[].name | string | 상품명                                             |
| products[].price | integer | 가격 (원)                                          |
| products[].status | string | 상품 상태 (`ACTIVE`,`OUT_OF_STOCK`, `DISCONTINUED`) |
| products[].createdAt | string | 상품 등록 일시 (ISO 8601)                             |
| products[].stock | string | 상품 수량                                           |
| pagination.page | integer | 현재 페이지 번호                                       |
| pagination.size | integer | 페이지 크기                                          |
| pagination.totalElements | integer | 전체 상품 수                                         |
| pagination.totalPages | integer | 전체 페이지 수                                        |
| pagination.hasNext | boolean | 다음 페이지 존재 여부                                    |
| pagination.hasPrevious | boolean | 이전 페이지 존재 여부                                    |


**예외 응답:**

| HTTP Status | error.code | message |
|-------------|-----------|---------|
| 400 | `INVALID_REQUEST` | 잘못된 요청입니다 |


---


### 1.2 상품 상세 조회

**Endpoint:** `GET /products/{productId}`

**설명:** 특정 상품의 상세 정보를 조회합니다.

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:----:|------|
| productId | integer | Y | 상품 ID |

**성공 응답:** `200 OK`
```json

{
  "status" : 200,
  "data": {
    "id": 1,
    "name": "최고심인형",
    "price": 19000,
    "createdAt": "2025-10-20T10:00:00Z",
    "status" : "ACTIVE"
  },
  "error": null,
  "timestamp": "2025-10-28T12:00:00Z"
}



```

#### Response 필드 설명

| 필드 | 타입 | 설명 |
|-----|------|------|
| id | integer | 상품 ID |
| name | string | 상품명 |
| price | integer | 가격 (원) |
| status | string | 상품 상태 (`ACTIVE`, `OUT_OF_STOCK`, `DISCONTINUED`) |
| createdAt | string | 상품 등록 일시 (ISO 8601) |



**예외 응답:**
```json

{
  "status": 400,
  "data": null,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "상품을 찾을 수 없습니다.",
    "details": {
      "requestedQuantity": 10,
      "availableQuantity": 5
    }
  },
  "timestamp": "2025-10-28T12:00:00Z"
}
```

| HTTP Status | error.code | message |
|-------------|-----------|---------|
| 404 | `PRODUCT_NOT_FOUND` | 상품을 찾을 수 없습니다 |

---

### 1.3 인기 상품 조회

**Endpoint:** `GET /products/popular`

**설명:** 최근 3일간 주문량 기준 상위 5개 인기 상품을 조회합니다.


**성공 응답:** `200 OK`
```json
{
  "status" : 200,
  "data": {
    "products": [
      {
        "id": 1,
        "name": "최고심인형",
        "price": 19000,
        "createdAt": "2025-10-20T10:00:00Z",
        "status" : "ACTIVE",
        "ranking": 2,
      },
      {
        "id": 1,
        "name": "미피인형",
        "price": 29000,
        "createdAt": "2025-10-20T10:00:00Z",
        "status" : "OUT_OF_STOCK",
        "ranking": 1,
      }
    ],
    "period": {
      "startDate": "2025-10-25T00:00:00Z",
      "endDate": "2025-10-28T23:59:59Z"
    }
  },
  "error": null,
  "timestamp": "2025-10-28T12:00:00Z"
}
```
#### Response 필드 설명

| 필드 | 타입 | 설명 |
|-----|------|------|
| products | array | 인기 상품 목록 (최대 5개) |
| products[].id | integer | 상품 ID |
| products[].name | string | 상품명 |
| products[].price | integer | 가격 (원) |
| products[].status | string | 상품 상태 |
| products[].ranking | integer | 순위 (1-5) |
| products[].createdAt | string | 상품 등록 일시 |
| period.startDate | string | 집계 시작 일시 |
| period.endDate | string | 집계 종료 일시 |


---

## 2. 장바구니 API

### 2.1 장바구니 조회

**Endpoint:** `GET /cart`

**설명:** 현재 사용자의 장바구니 내역을 조회합니다.

**Request Body:**
```json
{
  "userId" : 1,
}
```

**필드 설명:**

| 필드 | 타입 | 필수 | 설명        |
|-----|------|------|-----------|
| userId | integer | Y | 유저 ID     |

**성공 응답:** `200 OK`
```json

{
  "status" : 200,
  "data": {
    "userId" : 1,
    "items": [
      {
        "productId": 1,
        "productName": "최고심인형",
        "price": 19000,
        "quantity": 2,
        "subtotal": 38000
      },
      {
        "productId": 3,
        "productName": "의자",
        "price": 150000,
        "quantity": 1,
        "subtotal": 150000
      }
    ],
    "totalAmount": 188000,
    "totalItems": 2
  },
  "error": null,
  "timestamp": "2025-10-28T12:00:00Z"
}

```
#### Response 필드 설명

| 필드                  | 타입 | 설명          |
|---------------------|------|-------------|
| userId              | array | 유저 ID       |
| items               | array | 장바구니 아이템 목록 |
| items[].cartItemId  | integer | 장바구니 아이템 ID |
| items[].productId   | integer | 상품 ID       |
| items[].productName | string | 상품명         |
| items[].price       | integer | 단가 (원)      |
| items[].quantity    | integer | 수량          |
| items[].subtotal    | integer | 소계 (원)      |
| totalAmount         | integer | 총 금액 (원)    |
| totalItems          | integer | 총 상품 종류 수   |

---

### 2.2 장바구니 담기

**Endpoint:** `POST /cart/items`

**설명:** 장바구니에 상품을 추가합니다.

**Request Body:**
```json
{
  "userId" : 1,
  "productId": 1,
  "quantity": 2
}
```

**필드 설명:**

| 필드 | 타입 | 필수 | 설명        |
|-----|------|------|-----------|
| userId | integer | Y | 유저 ID     |
| productId | integer | Y | 상품 ID     |
| quantity | integer | Y | 수량 (1 이상) |

**성공 응답:** `201 Created`
```json
{
  "status" : 200,
  "data": {
    "userId" : 1
    "productId": 1,
    "quantity": 2,
  },
  "error": null,
  "timestamp": "2025-10-28T12:00:00Z"
}
```

#### Response 필드 설명

| 필드 | 타입 | 설명    |
|-----|------|-------|
| userId | integer | 유저 ID |
| productId | integer | 상품 ID |
| quantity | integer | 수량    |

**예외 응답:**

```json
{
  "status": 400,
  "data": null,
  "error": {
    "code": "STOCK_INSUFFICIENT",
    "message": "재고가 부족합니다",
    "details": {
      "requestedQuantity": 10,
      "availableQuantity": 5
    }
  },
  "timestamp": "2025-10-28T12:00:00Z"
}
```
| HTTP Status | error.code | message |
|-------------|-----------|---------|
| 400 | `VALIDATION_FAILED` | 입력값이 올바르지 않습니다 |
| 404 | `PRODUCT_NOT_FOUND` | 상품을 찾을 수 없습니다 |
| 400 | `STOCK_INSUFFICIENT` | 재고가 부족합니다 |

---

### 2.3 장바구니 상품 수량 변경

**Endpoint:** `PATCH /cart/items/{itemId}`

**설명:** 장바구니 상품의 수량을 변경합니다.

**Path Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| productId | integer | Y | 장바구니 아이템 ID |
| quantity | integer | Y | 수량  수량 (1 이상)|

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 3
}
```

**성공 응답:** `200 OK`
```json
{
  "status" : 200,
  "data": {
    "productId": 1,
    "quantity": 1,
  },
  "error": null,
  "timestamp": "2025-10-28T12:00:00Z"
}
```
#### Response 필드 설명

| 필드 | 타입 | 설명 |
|-----|------|------|
| productId | integer | 상품 ID |
| quantity | integer | 변경된 수량 |


**예외 응답:**

```json
{
  "status": 400,
  "data": null,
  "error": {
    "code": "STOCK_INSUFFICIENT",
    "message": "재고가 부족합니다",
    "details": {
      "productId": 1,
      "requestedQuantity": 10,
      "availableQuantity": 5
    }
  },
  "timestamp": "2025-10-28T12:00:00Z"
}
```

| HTTP Status | error.code | message |
|-------------|-----------|---------|
| 400 | `VALIDATION_FAILED` | 입력값이 올바르지 않습니다 |
| 404 | `PRODUCT_NOT_FOUND` | 상품을 찾을 수 없습니다 |
| 400 | `STOCK_INSUFFICIENT` | 재고가 부족합니다 |


---


### 2.4 장바구니 상품 삭제

**Endpoint:** `DELETE /cart/items/{itemId}`

**설명:** 장바구니에서 상품을 삭제합니다.

**Path Parameters:**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| itemId | integer | Y | 장바구니 아이템 ID |


**성공 응답:** `204 No Content`
```json
{
  "status" : 204,
  "data": {
    "id": 1,
    "price": 19000,
    "quantity": 1,
    "subtotal": 19000
  },
  "error": null,
  "timestamp": "2025-10-28T12:00:00Z"
}
```

**예외 응답:**

| HTTP Status | error.code | message |
|-------------|-----------|---------|
| 404 | `CART_ITEM_NOT_FOUND` | 장바구니 아이템을 찾을 수 없습니다 |

---

## 3. 주문 API

### 3.1 주문 생성

**Endpoint:** `POST /orders`

**설명:** 주문을 생성합니다. 재고 확인 및 차감이 이루어지지만 결제는 별도로 진행합니다.

**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 1,
    },
    {
      "productId": 2,
      "quantity": 2
    }
  ],
  "couponId": 10
}
```

**Request 필드 설명:**

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| items | array | Y | 주문 상품 목록 (최소 1개) |
| items[].productId | integer | Y | 상품 ID |
| items[].quantity | integer | Y | 수량 (1 이상) |
| couponId | integer | N | 적용할 쿠폰 ID |

**성공 응답:** `201 Created`
```json
{
  "status" : 200,
  "data": {
    "orderId" : 1000,
    "userId": 100,
    "items": [
      {
        "productId": 1,
        "productName": "최고심인형",
        "price": 19000,
        "quantity": 2,
        "subtotal": 38000
      },
      {
        "productId": 3,
        "productName": "의자",
        "price": 150000,
        "quantity": 1,
        "subtotal": 150000
      }
    ],
    "couponId": 10,
    "status": "PENDING",
    "discountAmount": 10000,
    "totalAmount": 178000,
    "totalItems": 2
  },
  "error": null,
  "timestamp": "2025-10-28T12:00:00Z"
}
```
#### Response 필드 설명

| 필드 | 타입 | 설명                                          |
|-----|------|---------------------------------------------|
| orderId | integer | 주문 ID                                       |
| userId | integer | 사용자 ID                                      |
| items | array | 주문 상품 목록                                    |
| items[].productId | integer | 상품 ID                                       |
| items[].productName | string | 상품명                                         |
| items[].price | integer | 단가 (원)                                      |
| items[].quantity | integer | 수량                                          |
| items[].subtotal | integer | 소계 (원)                                      |
| totalAmount | integer | 총 금액 (원)                                    |
| discountAmount | integer | 할인 금액 (원)                                   |
| totalItems | integer | 총 수량 |                                       |
| couponId | integer | 사용된 쿠폰 ID (없으면 null)                        |
| status | string | 주문 상태 (`PENDING`, `COMPLETED`, `CANCELLED`) |


**예외 응답:**

```json
{
  "status": 400,
  "data": null,
  "error": {
    "code": "INSUFFICIENT_INVENTORY",
    "message": "일부 상품의 재고가 부족합니다.",
    "details": {
      "outOfStockProducts": [
        {
          "productId": 5,
          "productName": "스마트워치",
          "requested": 10,
          "available": 5
        }
      ]
    }
  },
  "timestamp": "2025-10-28T12:00:00Z"
}
```

| HTTP Status | error.code | message |
|-------------|-----------|---------|
| 400 | `INVALID_REQUEST` | 잘못된 요청입니다 |
| 404 | `PRODUCT_NOT_FOUND` | 상품을 찾을 수 없습니다 |
| 400 | `STOCK_INSUFFICIENT` | 재고가 부족합니다 |
| 404 | `COUPON_NOT_FOUND` | 쿠폰을 찾을 수 없습니다 |
| 400 | `MIN_ORDER_AMOUNT_NOT_MET` | 최소 주문 금액을 충족하지 못했습니다 |
| 400 | `COUPON_EXPIRED` | 만료된 쿠폰입니다 |
| 400 | `COUPON_ALREADY_USED` | 이미 사용된 쿠폰입니다 |

---

### 3.3 주문 상세 조회

**Endpoint:** `GET /orders/{orderId}`

**설명:** 특정 주문의 상세 정보를 조회합니다.

**Path Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | integer | Y | 주문 ID |

**성공 응답:** `200 OK`
```json
{
  "status" : 200,
  "data": {
    "orderId": 1001,
    "userId": 100,
    "items": [
      {
        "productId": 5,
        "productName": "스마트워치",
        "quantity": 1,
        "price": 299000,
        "subtotal": 299000,
      }
    ],
    "totalAmount": 557000,
    "discountAmount": 50000,
    "finalAmount": 507000,
    "status": "COMPLETED",
    "createdAt": "2025-10-28T12:00:00Z",
  },
  "error": null,
  "timestamp": "2025-10-28T12:10:00Z"
}
```


#### Response 필드 설명

| 필드 | 타입 | 설명 |
|-----|------|------|
| orderId | integer | 주문 ID |
| userId | integer | 사용자 ID |
| items | array | 주문 상품 목록 |
| items[].productId | integer | 상품 ID |
| items[].productName | string | 상품명 |
| items[].price | integer | 단가 (원) |
| items[].quantity | integer | 수량 |
| items[].subtotal | integer | 소계 (원) |
| totalAmount | integer | 총 금액 (원) |
| discountAmount | integer | 할인 금액 (원) |
| finalAmount | integer | 최종 결제 금액 (원) |
| couponId | integer | 사용된 쿠폰 ID |
| status | string | 주문 상태 |
| createdAt | string | 주문 생성 일시 |

#### 예외 응답

**404 Not Found 예시:**
```json
{
  "status": 404,
  "data": null,
  "error": {
    "code": "ORDER_NOT_FOUND",
    "message": "주문을 찾을 수 없습니다",
    "details": {
      "orderId": 9999
    }
  },
  "timestamp": "2025-10-28T12:10:00Z"
}
```

| HTTP Status | error.code | message |
|-------------|-----------|---------|
| 404 | `ORDER_NOT_FOUND` | 주문을 찾을 수 없습니다 |
| 403 | `FORBIDDEN` | 권한이 없습니다 |


---

## 4. 쿠폰 API

### 4.1 발급 가능한 쿠폰 목록

**Endpoint:** `GET /coupons`

**설명:** 현재 발급 가능한 쿠폰 목록을 조회합니다.

**성공 응답:** `200 OK`
```json
{
  "status": 200,
  "data": {
    "coupons": [
      {
        "id": 10,
        "name": "신규가입 5만원 할인",
        "remainingQuantity": 245,
        "expiresAt": "2025-11-30T23:59:59Z",
        "issuedAt": "2025-11-30T23:59:59Z",
      },
      {
        "id": 11,
        "name": "10% 할인 쿠폰",
        "totalQuantity": 500,
        "remainingQuantity": 0,
        "expiresAt": "2025-10-31T23:59:59Z",
        "issuedAt": "2025-10-25T23:59:59Z",
      }
    ]
  },
  "error": null,
  "timestamp": "2025-10-28T12:20:00Z"
}
```
#### Response 필드 설명

| 필드 | 타입 | 설명 |
|-----|------|------|
| coupons | array | 쿠폰 목록 |
| coupons[].couponId | integer | 쿠폰 ID |
| coupons[].name | string | 쿠폰명 |
| coupons[].totalQuantity | integer | 총 발급 수량 |
| coupons[].remainingQuantity | integer | 남은 수량 |
| coupons[].issuedAt | string | 발급 시작 일시 |
| coupons[].expiresAt | string | 만료 일시 |


---

### 4.2 선착순 쿠폰 발급

**Endpoint:** `POST /coupons/{couponId}/issue`

**설명:** 선착순으로 쿠폰을 발급받습니다.

**Path Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| couponId | integer | Y | 쿠폰 ID |

**성공 응답:** `201 Created`
```json
{
  "status" : 200,
  "data": {
    "userCouponId": 10001,
    "id": 10,
    "name": "신규가입 5만원 할인",
    "issuedAt": "2025-10-28T12:25:00Z",
    "expiresAt": "2025-11-30T23:59:59Z"
  },
  "error": null,
  "timestamp": "2025-10-28T12:00:00Z"
}

```

#### Response 필드 설명

| 필드             | 타입 | 설명 |
|----------------|------|------|
| userCouponId   | integer | 사용자 쿠폰 ID |
| id             | integer | 쿠폰 ID |
| name     | string | 쿠폰명 |
| issuedAt       | string | 발급 일시 |
| expiresAt      | string | 만료 일시 |

**예외 응답:**

```json
{
  "status" : 409,
  "data": null,
  "error": {
    "code": "COUPON_ALREADY_ISSUED",
    "message": "이미 발급받은 쿠폰입니다.",
    "details": {
      "couponId": 10,
      "issuedAt": "2025-10-20T10:00:00Z"
    }
  },
  "timestamp": "2025-10-28T12:25:00Z"
}
```
- `410 Gone`: 쿠폰 수량 소진
```json
{
  "status": 410,
  "data": null,
  "error": {
    "code": "COUPON_SOLD_OUT",
    "message": "쿠폰이 모두 소진되었습니다.",
    "details": {
      "couponId": 11,
      "totalQuantity": 500,
      "issuedCount": 500
    }
  },
  "timestamp": "2025-10-28T12:25:00Z"
}
```

#### 에러 응답

| HTTP Status | error.code | message |
|-------------|-----------|---------|
| 404 | `COUPON_NOT_FOUND` | 쿠폰을 찾을 수 없습니다 |
| 409 | `COUPON_ALREADY_ISSUED` | 이미 발급받은 쿠폰입니다 |
| 410 | `COUPON_SOLD_OUT` | 쿠폰이 모두 소진되었습니다 |

---

### 4.3  쿠폰 조회

**Endpoint:** `GET /users/coupons`

**설명:** 현재 사용자가 보유한 쿠폰 목록을 조회합니다.

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| status | string | N | 쿠폰 상태 (AVAILABLE, USED, EXPIRED) |

**성공 응답:** `200 OK`
```json
{
  "status": 200,
  "data": {
    "coupons": [
      {
        "userCouponId": 10001,
        "couponId": 10,
        "couponName": "신규가입 5만원 할인",
        "status": "AVAILABLE",
        "issuedAt": "2025-10-28T12:25:00Z",
        "expiresAt": "2025-11-30T23:59:59Z",
        "usedAt": null,
        "orderId": null
      },
      {
        "userCouponId": 10000,
        "couponId": 9,
        "couponName": "첫 구매 3만원 할인",
        "status": "USED",
        "issuedAt": "2025-10-20T10:00:00Z",
        "expiresAt": "2025-11-20T23:59:59Z",
        "usedAt": "2025-10-25T14:30:00Z",
        "orderId": 995
      }
    ]
  },
  "error": null,
  "timestamp": "2025-10-28T12:30:00Z"
}
```

#### Response 필드 설명

| 필드 | 타입 | 설명 |
|-----|------|------|
| coupons | array | 보유 쿠폰 목록 |
| coupons[].userCouponId | integer | 사용자 쿠폰 ID |
| coupons[].couponId | integer | 쿠폰 ID |
| coupons[].couponName | string | 쿠폰명 |
| coupons[].status | string | 쿠폰 상태 |
| coupons[].issuedAt | string | 발급 일시 |
| coupons[].expiresAt | string | 만료 일시 |
| coupons[].usedAt | string | 사용 일시 (미사용 시 null) |
| coupons[].orderId | integer | 사용된 주문 ID (미사용 시 null) |

### 4.4  쿠폰 적용 가능 여부 조회

**Endpoint:** `GET /coupons/validate`

**설명:** 주문 전 쿠폰 적용 가능 여부를 검증합니다.

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| couponId | integer | Y | 쿠폰 ID |

**성공 응답:** `200 OK`
```json
{
  "status": 200,
  "data": {
    "couponId": 10,
    "couponName": "신규가입 5만원 할인",
  },
  "error": null,
  "timestamp": "2025-10-28T12:35:00Z"
}
```


## 참고 사항

### 데이터 타입
- **날짜/시간**: ISO 8601 형식 (예: `2025-10-28T12:00:00Z`)
- **금액**: 정수형, 원화 기준 (예: 10000 = 10,000원)
- **ID**: 정수형 (Long)

### 페이지네이션
- 기본 페이지 크기: 20
- 최대 페이지 크기: 100
- 페이지 번호는 1부터 시작

### Rate Limiting
- IP당: 1000 요청/분
- 사용자당: 100 요청/분
- 쿠폰 발급 API: 10 요청/분


### 외부 데이터 전송
- 주문 완료 후 비동기로 외부 시스템에 전송
- 전송 실패 시에도 주문은 정상 처리
- 실패한 전송은 재시도 큐에 등록
