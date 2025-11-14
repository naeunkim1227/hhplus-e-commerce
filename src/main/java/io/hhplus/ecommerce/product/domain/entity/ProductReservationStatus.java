package io.hhplus.ecommerce.product.domain.entity;

public enum ProductReservationStatus {
    ACTIVE("활성"),
    CONFIRMED("확정"),
    RELEASED("해제"),
    EXPIRED("만료")  ;

    private final String description;

    ProductReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
