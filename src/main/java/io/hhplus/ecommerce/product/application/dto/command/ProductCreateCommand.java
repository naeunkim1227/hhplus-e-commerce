package io.hhplus.ecommerce.product.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ProductCreateCommand {
    private String name;
    private BigDecimal price;
    private Long stock;
}
