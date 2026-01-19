package com.fluxmall.wishlist.dto.request;

import jakarta.validation.constraints.NotNull;

public record WishlistToggleRequest(
        @NotNull(message = "상품 ID는 필수입니다")
        Long productId
) {
}
