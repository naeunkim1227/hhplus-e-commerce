package io.hhplus.ecommerce.product.presentation.dto.request;

import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    private Long id;

    @NotBlank(message = "상품명은 필수입니다")
    private String name;

    @NotNull
    @Positive(message = "가격은 0보다 커야 합니다")
    private BigDecimal price;

    @NotNull
    @Positive(message = "재고는 0보다 커야 합니다")
    private Long stock;

    public ProductCreateCommand toCommand() {
        return ProductCreateCommand.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .build();
    }
}
