package com.fluxmall.product.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @Size(max = 200, message = "상품명은 200자 이하입니다")
        String productName,

        @Size(max = 2000, message = "설명은 2000자 이하입니다")
        String description,

        @Size(max = 50, message = "카테고리는 50자 이하입니다")
        String category,

        @Positive(message = "가격은 양수여야 합니다")
        Integer price,

        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다")
        Integer stockQuantity
) {
}
