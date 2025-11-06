package io.hhplus.ecommerce.product.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 검색 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    private String keyword;
    private String status;
    private Integer page;
    private Integer size;
}