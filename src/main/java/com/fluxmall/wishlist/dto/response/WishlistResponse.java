package com.fluxmall.wishlist.dto.response;

import com.fluxmall.wishlist.domain.Wishlist;

import java.time.LocalDateTime;

public record WishlistResponse(
        Long wishlistId,
        Long productId,
        String productName,
        Integer productPrice,
        String productStatus,
        LocalDateTime createdAt
) {
    public static WishlistResponse from(Wishlist wishlist) {
        return new WishlistResponse(
                wishlist.getId(),
                wishlist.getProductId(),
                wishlist.getProductName(),
                wishlist.getProductPrice(),
                wishlist.getProductStatus(),
                wishlist.getCreatedAt()
        );
    }
}
