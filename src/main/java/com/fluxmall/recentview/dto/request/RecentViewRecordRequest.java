package com.fluxmall.recentview.dto.request;

import jakarta.validation.constraints.NotNull;

public record RecentViewRecordRequest(
        @NotNull(message = "상품 ID는 필수입니다")
        Long productId
) {
}
