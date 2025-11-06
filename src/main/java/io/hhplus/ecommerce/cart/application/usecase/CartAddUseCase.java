package io.hhplus.ecommerce.cart.application.usecase;

import io.hhplus.ecommerce.cart.application.dto.result.CartItemDto;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 장바구니 담기 UseCase
 * 1. 상품 존재 여부 확인
 * 2. 사용 가능한 재고 확인 (전체 재고 - 선점 재고)
 * 3. 장바구니에 추가 (이미 있으면 수량 증가)
 */
@Service
@RequiredArgsConstructor
public class CartAddUseCase {

    private final CartService cartService;
    private final ProductService productService;

    public CartItemDto execute(CartItemAddCommand command) {
        Product product = productService.getProduct(command.getProductId());

        if (!productService.isStockAvailableForReservation(command.getProductId(), command.getQuantity())) {
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK);
        }

        // 3. 장바구니에 추가
        CartItem cartItem = cartService.addOrUpdateCartItem(command);

        return CartItemDto.from(cartItem, product);
    }
}

