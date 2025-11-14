package io.hhplus.ecommerce.cart.application.usecase;

import io.hhplus.ecommerce.cart.application.dto.result.CartItemDto;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemUpdateCommand;
import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartUpdateUseCase {

    private final CartService cartService;

    public CartItemDto excute(CartItemUpdateCommand command) {
        CartItem cartItem = cartService.updateCartItem(command);
        return CartItemDto.from(cartItem);
    }
}
