# 3. 🗄️ 데이터 모델

## 1. Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    USER ||--o{ CART_ITEM : "has"
    USER ||--o{ ORDER : "places"
    USER ||--o{ USER_COUPON : "owns"
    USER ||--o{ POINT_HISTORY : "has"
    
    PRODUCT ||--o{ CART_ITEM : "in"
    PRODUCT ||--o{ ORDER_ITEM : "in"
    PRODUCT ||--o{ PRODUCT_RESERVATION : "reserved"
    
    ORDER ||--o{ ORDER_ITEM : "contains"
    ORDER ||--o{ PRODUCT_RESERVATION : "reserves"
    ORDER ||--o| COUPON : "uses"
    
    COUPON ||--o{ USER_COUPON : "issued to"

    USER {
        bigint id PK
        varchar(100) name "NOT NULL"
        decimal(15_2) balance
        datetime created_at "NOT NULL, DEFAULT now()"
        datetime updated_at "NOT NULL, DEFAULT now()"
    }

    PRODUCT {
        bigint id PK
        varchar(100) name "NOT NULL"
        decimal(15_2) price "NOT NULL"
        bigint stock
        varchar status "ACTIVE/INACTIVE/OUT_OF_STOCK/DISCONTINUED"
        datetime created_at "NOT NULL, DEFAULT now()"
        datetime updated_at "NOT NULL, DEFAULT now()"
    }

    CART_ITEM {
        bigint id PK
        bigint user_id 
        bigint product_id 
        int quantity
        datetime created_at "NOT NULL, DEFAULT now()"
        datetime updated_at "NOT NULL, DEFAULT now()"
    }

    ORDER {
        bigint id PK
        bigint user_id 
        varchar(100) status "PENDING/PAYMENT_COMPLETED/PREPARING/SHIPPING/DELIVERED/CANCELLED"
        bigint coupon_id 
        decimal(15_2) total_amount
        decimal(15_2) discount_amount
        decimal(15_2) final_amount
        datetime created_at "NOT NULL, DEFAULT now()"
        datetime orderd_at
        datetime updated_at "NOT NULL, DEFAULT now()"
    }

    ORDER_ITEM {
        bigint id PK
        bigint order_id 
        bigint product_id 
        int quantity
        decimal(15_2) price
        datetime created_at "NOT NULL, DEFAULT now()"
    }

    COUPON {
        bigint id PK
        varchar(100) code
        varchar(100) name
        varchar(100) type "RATE/STATIC"
        야
        int total_quantity
        int issued_quantity
        datetime start_date
        datetime end_date
        varchar(100) status "ACTIVE/INACTIVE/EXPIRED/SOLD_OUT"
        datetime created_at "NOT NULL, DEFAULT now()"
        datetime updated_at "NOT NULL, DEFAULT now()"
    }

    USER_COUPON {
        bigint id PK
        bigint user_id  "NOT NULL"
        bigint coupon_id 
        bigint status 
        datetime issued_at
        datetime used_at    
        datetime expired_at
    }

    POINT_HISTORY {
        bigint id PK
        bigint user_id 
        varchar type "EARNED/USED/EXPIRED/REFUNDED"
        decimal(15_2) amount
        datetime created_at "NOT NULL, DEFAULT now()"
        datetime updated_at "NOT NULL, DEFAULT now()"
    }

    PRODUCT_RESERVATION {
        bigint id PK
        bigint order_id  "NOT NULL"
        bigint product_id  "NOT NULL"
        int stock "NOT NULL"
        varchar(20) status "NOT NULL, RESERVED/CONFIRMED/RELEASED/EXPIRED"
        datetime reserved_at "NOT NULL, DEFAULT now()"
        datetime expired_at "NOT NULL"
        datetime released_atdatetime created_at "NOT NULL, DEFAULT now()"
        datetime updated_at "NOT NULL, DEFAULT now()"
    }


```

---
## 2. DDL 명세 

Docs: https://dbdiagram.io/d/6900f304357668b7320840d5

```sql

CREATE TABLE `user` (
                        `id` bigint PRIMARY KEY,
                        `name` varchar(100) NOT NULL,
                        `balance` decimal(15,2),
                        `created_at` datetime NOT NULL DEFAULT (now()),
                        `updated_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `product` (
                           `id` bigint PRIMARY KEY,
                           `name` varchar(100) NOT NULL,
                           `price` decimal(15,2) NOT NULL,
                           `stock` bigint,
                           `status` varchar(255),
                           `version` bigint NOT NULL,
                           `created_at` datetime NOT NULL DEFAULT (now()),
                           `updated_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `cart_item` (
                             `id` bigint,
                             `user_id` bigint,
                             `product_id` bigint,
                             `quantity` int,
                             `created_at` datetime NOT NULL DEFAULT (now()),
                             `updated_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `order` (
                         `id` bigint PRIMARY KEY,
                         `user_id` bigint,
                         `status` varchar(100),
                         `coupon_id` bigint,
                         `total_amount` decimal(15,2),
                         `discount_amount` decimal(15,2),
                         `final_amount` decimal(15,2),
                         `created_at` datetime NOT NULL DEFAULT (now()),
                         `orderd_at` datetime,
                         `updated_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `order_item` (
                              `id` bigint PRIMARY KEY,
                              `order_id` bigint,
                              `product_id` bigint,
                              `quantity` int,
                              `price` decimal(15,2),
                              `created_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `coupon` (
                          `id` bigint PRIMARY KEY,
                          `code` varchar(100),
                          `name` varchar(100),
                          `total_quantity` int,
                          `issued_quantity` int,
                          `start_date` datetime,
                          `end_date` datetime,
                          `status` varchar(100),
                          `version` bigint NOT NULL,
                          `created_at` datetime NOT NULL DEFAULT (now()),
                          `updated_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `user_coupon` (
                               `id` bigint PRIMARY KEY,
                               `user_id` bigint NOT NULL,
                               `coupon_id` bigint,
                               `status` varchar(100),
                               `issued_at` datetime,
                               `used_at` datetime,
                               `expired_at` datetime
);

CREATE TABLE `point_history` (
                                 `id` bigint PRIMARY KEY,
                                 `user_id` bigint,
                                 `type` varchar(100),
                                 `amount` decimal(15,2),
                                 `created_at` datetime NOT NULL DEFAULT (now()),
                                 `updated_at` datetime NOT NULL DEFAULT (now())
);

CREATE TABLE `product_reservation` (
                                       `id` bigint PRIMARY KEY,
                                       `order_id` bigint NOT NULL,
                                       `product_id` bigint NOT NULL,
                                       `quantity` int NOT NULL,
                                       `status` varchar(100) NOT NULL,
                    
                                       `reserved_at` datetime NOT NULL DEFAULT (now()),
                                       `expired_at` datetime NOT NULL,
                                       `released_at` datetime,
                                       `created_at` datetime NOT NULL DEFAULT (now()),
                                       `updated_at` datetime NOT NULL DEFAULT (now())
);

CREATE INDEX `idx_order_user` ON `order` (`user_id`);

CREATE INDEX `idx_order_status` ON `order` (`status`);

CREATE INDEX `idx_order_user_status` ON `order` (`user_id`, `status`);

CREATE UNIQUE INDEX `user_coupon_index_3` ON `user_coupon` (`user_id`, `coupon_id`);

CREATE INDEX `idx_stock_reservation_order` ON `product_reservation` (`order_id`);

CREATE INDEX `idx_stock_reservation_product` ON `product_reservation` (`product_id`);

CREATE INDEX `idx_stock_reservation_product_status` ON `product_reservation` (`product_id`, `status`);

ALTER TABLE `product` COMMENT = 'Status values:
- ACTIVE: 판매 중
- INACTIVE: 판매 중지
- DISCONTINUED: 단종
';

ALTER TABLE `order` COMMENT = 'Status values:
- PENDING: 결제 대기
- PAYMENT_COMPLETED: 결제 완료
- PREPARING: 배송 준비
- SHIPPING: 배송 중
- DELIVERED: 배송 완료
- CANCELLED: 취소됨
';

ALTER TABLE `coupon` COMMENT = 'Status values:
- ACTIVE: 발급 가능
- INACTIVE: 발급 중지
- EXPIRED: 기간 만료
- SOLD_OUT: 소진됨
';

ALTER TABLE `user_coupon` COMMENT = 'Status values:
- AVAILABLE: 사용 가능
- USED: 사용 완료
- EXPIRED: 만료됨
';

ALTER TABLE `point_history` COMMENT = 'Transaction Type:
- EARNED: 적립
- USED: 사용
- EXPIRED: 소멸
- REFUNDED: 환불 적립
';

ALTER TABLE `product_reservation` COMMENT = '재고 예약 상태:
- RESERVED: 예약 중 (주문 생성 후 15분간 유지)
- CONFIRMED: 확정됨 (결제 완료)
- RELEASED: 해제됨 (주문 취소, 시간 만료)
- EXPIRED: 만료됨 (15분 초과)
';

ALTER TABLE `product_reservation` ADD FOREIGN KEY (`order_id`) REFERENCES `order` (`id`);

ALTER TABLE `product_reservation` ADD FOREIGN KEY (`product_id`) REFERENCES `product` (`id`);

ALTER TABLE `cart_item` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `cart_item` ADD FOREIGN KEY (`product_id`) REFERENCES `product` (`id`);

ALTER TABLE `order` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `order` ADD FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`);

ALTER TABLE `order_item` ADD FOREIGN KEY (`order_id`) REFERENCES `order` (`id`);

ALTER TABLE `user_coupon` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `user_coupon` ADD FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`);

ALTER TABLE `point_history` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

```