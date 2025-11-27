package io.hhplus.ecommerce.product.domain.validator;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.exception.CartErrorCode;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.domain.ProductPolicy;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ProductValidator {

    /**
     * 조회 조건
     */
    public void searchCondition(int days){
        if(days > ProductPolicy.MAX_SEARCH_DAY){
            throw new BusinessException(ProductErrorCode.SEARCH_CONDITION_LIMIT);
        }
    }

}
