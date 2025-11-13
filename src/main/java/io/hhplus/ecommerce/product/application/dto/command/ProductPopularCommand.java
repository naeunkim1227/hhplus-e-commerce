package io.hhplus.ecommerce.product.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ProductPopularCommand {
    private Integer days;
    private Integer limit;
}
