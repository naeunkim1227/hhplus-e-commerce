package io.hhplus.ecommerce.cart.domain.validator;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.exception.CartErrorCode;
import io.hhplus.ecommerce.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class CartValidator {

    public void validate(Long userId, CartItem cartItem) {
        validateCollectUser(userId,cartItem);
    }
    /**
     * 추가하려는 카트의 소유자인지 확인
     */
    public void validateCollectUser(Long userId, CartItem cartItem){
        if(!userId.equals(cartItem.getUserId())){
            throw new BusinessException(CartErrorCode.CART_ITEM_NOW_ALLOWED);
        }
    }


}
