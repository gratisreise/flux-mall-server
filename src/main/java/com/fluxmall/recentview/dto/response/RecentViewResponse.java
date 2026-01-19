package com.fluxmall.recentview.dto.response;

import com.fluxmall.recentview.domain.RecentView;

import java.time.LocalDateTime;

public record RecentViewResponse(
        Long recentViewId,
        Long productId,
        String productName,
        Integer productPrice,
        String productStatus,
        LocalDateTime viewedAt
) {
    public static RecentViewResponse from(RecentView recentView) {
        return new RecentViewResponse(
                recentView.getId(),
                recentView.getProductId(),
                recentView.getProductName(),
                recentView.getProductPrice(),
                recentView.getProductStatus(),
                recentView.getViewedAt()
        );
    }
}
