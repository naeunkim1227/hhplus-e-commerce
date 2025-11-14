package io.hhplus.ecommerce.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    private Long productId;
    private int quantity;
    private BigDecimal price;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Order order;

    public static OrderItem create(
            Long productId, BigDecimal price, int quantity
    ) {
        return OrderItem.builder()
                .productId(productId)
                .quantity(quantity)
                .price(price)  // 구매 시점의 상품 가격 저장
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}