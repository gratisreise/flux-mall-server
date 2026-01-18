package com.fluxmall.product.dto.request;

import com.fluxmall.product.domain.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 200, message = "상품명은 200자 이하입니다")
        String productName,

        @Size(max = 2000, message = "설명은 2000자 이하입니다")
        String description,

        @NotBlank(message = "카테고리는 필수입니다")
        @Size(max = 50, message = "카테고리는 50자 이하입니다")
        String category,

        @NotNull(message = "가격은 필수입니다")
        @Positive(message = "가격은 양수여야 합니다")
        Integer price,

        @NotNull(message = "재고 수량은 필수입니다")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다")
        Integer stockQuantity
) {
    public Product toEntity(Long memberId) {
        return Product.builder()
                .memberId(memberId)
                .productName(productName)
                .description(description)
                .category(category)
                .price(price)
                .stockQuantity(stockQuantity)
                .productStatus(Product.ProductStatus.ON_SALE)
                .build();
    }
}
