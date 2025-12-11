package io.hhplus.ecommerce.cart.application.usecase;

import io.hhplus.ecommerce.cart.application.dto.result.CartItemDto;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.event.CartAddedEvent;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장바구니 담기 UseCase
 * 1. 상품 존재 여부 확인
 * 2. 재고 확인
 * 3. 장바구니에 추가 (이미 있으면 수량 증가)
 */
@Service
@RequiredArgsConstructor
public class CartAddUseCase {

    private final CartService cartService;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CartItemDto execute(CartItemAddCommand command) {
        Product product = productService.getProduct(command.getProductId());

        // 재고 확인
        productService.validate(product, command.getQuantity());

        // 장바구니에 추가
        CartItem cartItem = cartService.addOrUpdateCartItem(command);

        eventPublisher.publishEvent(new CartAddedEvent(command.getUserId(),command.getProductId(),command.getQuantity()));
        return CartItemDto.from(cartItem, product);
    }
}

