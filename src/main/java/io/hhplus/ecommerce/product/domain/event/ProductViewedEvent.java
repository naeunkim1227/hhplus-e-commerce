package io.hhplus.ecommerce.product.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductViewedEvent {
    private Long productId;
    private Long userId;
}
