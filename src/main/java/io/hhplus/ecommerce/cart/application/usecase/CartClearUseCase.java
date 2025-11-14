package io.hhplus.ecommerce.cart.application.usecase;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemDeleteCommand;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartClearUseCase {

    private final CartService cartService;

    public void excute(CartItemDeleteCommand command) {
        cartService.clearCart(command.getUserId());
    }

}
