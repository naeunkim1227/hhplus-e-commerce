package io.hhplus.ecommerce.product.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductUnLikedEvent {
    private Long productId;
    private Long userId;
}
