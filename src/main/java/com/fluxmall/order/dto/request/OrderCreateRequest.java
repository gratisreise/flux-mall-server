package com.fluxmall.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderCreateRequest(
        @NotEmpty(message = "주문할 상품이 없습니다")
        @Valid
        List<OrderItemCreateRequest> items,

        @NotBlank(message = "배송지는 필수입니다")
        @Size(max = 100, message = "배송지는 100자 이하여야 합니다")
        String shippingAddress
) {
}
