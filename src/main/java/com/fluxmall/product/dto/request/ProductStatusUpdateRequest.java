package com.fluxmall.product.dto.request;

import com.fluxmall.product.domain.Product;
import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(
        @NotNull(message = "상품 상태는 필수입니다")
        Product.ProductStatus productStatus
) {
}
