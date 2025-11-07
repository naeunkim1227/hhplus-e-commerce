package io.hhplus.ecommerce.cart.application.usecase;

import io.hhplus.ecommerce.cart.application.dto.result.CartItemDto;
import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 장바구니 조회 UseCase
 */
@Service
@RequiredArgsConstructor
public class CartGetUseCase {
    private final CartService cartService;
    private final ProductService productService;

    /**
     * 사용자의 장바구니 조회
     */
    @Transactional(readOnly = true)
    public List<CartItemDto> execute(Long userId) {
        List<CartItem> cartItems = cartService.getCartItems(userId);

        if (cartItems.isEmpty()) {
            return List.of();
        }

        return cartItems.stream()
                .map(cartItem -> {
                    Product product = productService.getProduct(cartItem.getProductId());
                    return CartItemDto.from(cartItem, product);
                })
                .toList();
    }
}
