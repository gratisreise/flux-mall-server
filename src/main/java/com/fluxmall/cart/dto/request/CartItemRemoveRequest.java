package com.fluxmall.cart.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CartItemRemoveRequest(
        @NotEmpty(message = "삭제할 상품을 선택해주세요")
        List<Long> cartItemIds
) {
}
