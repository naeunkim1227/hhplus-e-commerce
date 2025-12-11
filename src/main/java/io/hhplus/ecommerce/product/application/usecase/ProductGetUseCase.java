package io.hhplus.ecommerce.product.application.usecase;

import io.hhplus.ecommerce.product.application.dto.result.ProductDto;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.event.ProductLikedEvent;
import io.hhplus.ecommerce.product.domain.event.ProductViewedEvent;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import io.hhplus.ecommerce.ranking.domain.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductGetUseCase {
    private final ProductService productService;
    private final RankingService rankingService;
    private final ApplicationEventPublisher eventPublisher;
    /**
     * 상품 상세 조회
     */
    public ProductDto execute(Long productId, Long userId) {
        Product product = productService.getProduct(productId);
        int likeCount = productService.getLikeCount(productId);

        eventPublisher.publishEvent(new ProductViewedEvent(productId,userId));
        return ProductDto.from(product,likeCount);
    }
}
