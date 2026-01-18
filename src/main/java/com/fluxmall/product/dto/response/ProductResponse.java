package com.fluxmall.product.dto.response;

import com.fluxmall.product.domain.Product;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long memberId,
        String productName,
        String description,
        String category,
        Integer price,
        Integer stockQuantity,
        Product.ProductStatus productStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getMemberId(),
                product.getProductName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getProductStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
