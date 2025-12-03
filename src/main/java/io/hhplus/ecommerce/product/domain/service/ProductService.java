package io.hhplus.ecommerce.product.domain.service;

import io.hhplus.ecommerce.common.config.CacheType;
import io.hhplus.ecommerce.common.config.redis.RedisKeyType;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.common.lock.DistributedLock;
import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.application.dto.command.ProductPopularCommand;
import io.hhplus.ecommerce.product.application.dto.command.ProductUpdateCommand;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import io.hhplus.ecommerce.product.domain.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final int POPULAR_PRODUCTS_LIMIT = 100;

    /**
     * 상품 ID로 상품 조회 - 캐시 적용
     */
    @Cacheable(cacheNames = CacheType.Names.PRODUCTS, key = "#productId")
    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 여러 상품 ID로 상품 일괄 조회 (N+1 방지)
     */
    public List<Product> getProductsByIds(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }

    /**
     * 재고 확인
     */
    public void validate(Product product, int quantity){
        if(product.getStock() < quantity){
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
    }

    /**
     * 모든 상품 조회
     */
    @Cacheable(cacheNames = CacheType.Names.PRODUCTS, key = "'all'")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * 최근 N일 동안 가장 판매량이 많은 상품 조회 - 캐시 적용
     */
    public List<Product> getPopularProducts(@Valid ProductPopularCommand command) {
        LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(command.getDays());
        return productRepository.findPopularProducts(startDate, command.getLimit());
    }

    /***
     * 일간 판매량 조회 limit 100개
     */
    @Cacheable(cacheNames = CacheType.Names.POPULAR_PRODUCTS, key = "'daily'" )
    public List<Product> getPopularProductsDaily() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        return productRepository.findPopularProducts(startDate, POPULAR_PRODUCTS_LIMIT);
    }

    /***
     * 주간 판매량 조회 limit 100개
     */
    @Cacheable(cacheNames = CacheType.Names.POPULAR_PRODUCTS,  key = "'weekly'")
    public List<Product> getPopularProductsWeekly() {
        LocalDateTime startDate = LocalDateTime.now().minusWeeks(1);
        return productRepository.findPopularProducts(startDate, POPULAR_PRODUCTS_LIMIT);
    }

    /***
     * 월간 판매량 조회 limit 100개
     */
    @Cacheable(cacheNames = CacheType.Names.POPULAR_PRODUCTS, key = "'monthly'")
    public List<Product> getPopularProductsMonthly() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        return productRepository.findPopularProducts(startDate, POPULAR_PRODUCTS_LIMIT);
    }

    /**
     * 상품 생성
     */
    public Product createProduct(@Valid ProductCreateCommand command) {
        Product product =  Product.create(command);
        return productRepository.save(product);
    }

    /**
     * 재고 차감 - 분산락 적용
     */
    @DistributedLock(key = "'product:stock:' + #productId" , waitTime = 5, leaseTime = 10)
    @Transactional(propagation = Propagation.REQUIRES_NEW,  timeout = 8 )
    public void decreaseStock(Long productId, int quantity) {
        int updated = productRepository.decreaseStock(productId, quantity);
        if (updated == 0) {
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
    }

    /**
     * 재고 증가 (결제 실패 시 복구용)
     * @param productId 상품 ID
     * @param quantity 증가할 수량
     */
    @Transactional
    public void increaseStock(Long productId, int quantity) {
        productRepository.increaseStock(productId, quantity);
    }

    /**
     * 상품 정보 수정 - 각 필드 선택적 업데이트
     */
    @CacheEvict(cacheNames = CacheType.Names.PRODUCTS, allEntries = true)
    @Transactional
    public Product updateProduct(ProductUpdateCommand command) {
        Product product = this.getProduct(command.getProductId());
        // 상품 정보 업데이트 (null이 아닌 필드만 업데이트)
        Product updatedProduct = Product.builder()
                .id(product.getId())
                .name(command.getName() != null ? command.getName() : product.getName())
                .price(command.getPrice() != null ? command.getPrice() : product.getPrice())
                .stock(command.getStock() != null ? command.getStock() : product.getStock())
                .status(command.getStatus() != null ? command.getStatus() : product.getStatus())
                .likeCount(product.getLikeCount())
                .version(product.getVersion())
                .createdAt(product.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        return productRepository.save(updatedProduct);
    }

    public int getLikeCount(Long productId) {
        String countKey = RedisKeyType.PRODUCT_LIKE.getKey(productId);
        String count = redisTemplate.opsForValue().get(countKey);

        if (count != null) {
            return Integer.parseInt(count);
        }

        Product product = getProduct(productId);
        int dbLikeCount = product.getLikeCount();

        redisTemplate.opsForValue().set(countKey, String.valueOf(dbLikeCount));
        return dbLikeCount;
    }

    public boolean increaseLikeCount(Long productId,Long userId) {
        String productLikeKey = RedisKeyType.PRODUCT_LIKE.getKey(productId);
        String userLikeKey = RedisKeyType.USER_LIKES.getKey(userId);

        Long added = redisTemplate.opsForSet().add(userLikeKey, productId.toString());

        if (added == null || added == 0) {
            return false;
        }

        redisTemplate.opsForValue().increment(productLikeKey);
        return true;
    }

    public boolean decreaseLikeCount(Long productId, Long userId) {
        String productLikeKey = RedisKeyType.PRODUCT_LIKE.getKey(productId);
        String userLikeKey = RedisKeyType.USER_LIKES.getKey(userId);

        Long removed = redisTemplate.opsForSet().remove(userLikeKey, productId.toString());

        if (removed == null || removed == 0) {
            return false;
        }

        redisTemplate.opsForValue().decrement(productLikeKey);
        return true;
    }


}