package io.hhplus.ecommerce.cart.presentation.dto.response;

import io.hhplus.ecommerce.cart.application.dto.result.CartItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 장바구니 전체 조회 응답 DTO (Presentation Layer)
 * 여러 아이템을 포함하는 장바구니 전체 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long userId;
    private List<CartItemResponse> items;
    private Integer totalQuantity;
    private BigDecimal totalPrice;

    //List<CartItemDto> > response
    public static CartResponse from(Long userId,List<CartItemDto> cartDtos) {
        List<CartItemResponse> items = cartDtos.stream()
                .map(CartItemResponse::from)
                .toList();

        int totalQuantity = Math.toIntExact(cartDtos.stream()
                .mapToLong(CartItemDto::getQuantity)
                .sum());

        BigDecimal totalPrice = cartDtos.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .userId(userId)
                .items(items)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice)
                .build();
    }
}