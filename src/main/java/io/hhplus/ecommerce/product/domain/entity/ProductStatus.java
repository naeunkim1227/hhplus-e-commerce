package io.hhplus.ecommerce.product.domain.entity;

public enum ProductStatus {
    ACTIVE("판매중"),
    INACTIVE("판매중지"),
    DISCONTINUED("단종");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
