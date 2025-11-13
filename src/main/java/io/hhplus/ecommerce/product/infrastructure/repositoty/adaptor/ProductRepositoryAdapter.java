package io.hhplus.ecommerce.product.infrastructure.repositoty.adaptor;

import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.repository.ProductRepository;
import io.hhplus.ecommerce.product.infrastructure.repositoty.jpa.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaRepository;

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return jpaRepository.findAllById(productIds);
    }

    @Override
    public List<Product> findPopularProducts(LocalDateTime startDate, int limit) {
        return jpaRepository.findPopularProducts(startDate, limit);
    }
}