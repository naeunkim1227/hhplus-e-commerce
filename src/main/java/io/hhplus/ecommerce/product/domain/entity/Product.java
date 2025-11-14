package io.hhplus.ecommerce.product.domain.entity;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.common.exception.CommonErrorCode;
import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal price;
    private Long stock;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 상품 생성
    public static Product create(ProductCreateCommand command) {
        validateStock(command.getStock());
        return Product.builder()
                .name(command.getName())
                .price(command.getPrice())
                .stock(command.getStock())
                .status(ProductStatus.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 재고 감소
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ProductErrorCode.INVALID_QUANTITY);
        }
        if (this.stock < quantity) {
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }

    // 재고 증가
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ProductErrorCode.INVALID_QUANTITY);
        }
        this.stock += quantity;
    }

    // 상태 변경
    public void changeStatus(ProductStatus newStatus) {
        if (newStatus == null) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "상품 상태는 필수입니다");
        }
        this.status = newStatus;
    }

    // 재고 확인
    public boolean isStockAvailable(int quantity) {
        return this.stock >= quantity;
    }

    // 판매 가능 여부
    public boolean isAvailableForSale() {
        return this.status == ProductStatus.ACTIVE && this.stock > 0;
    }

    static void validateStock(Long stock) {
        if (stock == null || stock < 0) {
            throw new BusinessException(ProductErrorCode.INVALID_QUANTITY, "재고는 0 이상이어야 합니다");
        }
    }

}
