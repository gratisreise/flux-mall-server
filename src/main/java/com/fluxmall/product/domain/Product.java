package com.fluxmall.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private Long memberId;
    private String productName;
    private String description;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private ProductStatus productStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ProductStatus {
        ON_SALE, SOLD_OUT, DISCONTINUED
    }

    public void updateStock(int quantity) {
        this.stockQuantity = quantity;
        if (this.stockQuantity <= 0) {
            this.productStatus = ProductStatus.SOLD_OUT;
        }
    }

    public void decreaseStock(int quantity) {
        this.stockQuantity -= quantity;
        if (this.stockQuantity <= 0) {
            this.productStatus = ProductStatus.SOLD_OUT;
        }
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
        if (this.stockQuantity > 0 && this.productStatus == ProductStatus.SOLD_OUT) {
            this.productStatus = ProductStatus.ON_SALE;
        }
    }
}
