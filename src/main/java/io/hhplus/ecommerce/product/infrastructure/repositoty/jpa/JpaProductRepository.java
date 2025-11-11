package io.hhplus.ecommerce.product.infrastructure.repositoty.jpa;

import io.hhplus.ecommerce.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductRepository extends JpaRepository<Product, Long> {
}