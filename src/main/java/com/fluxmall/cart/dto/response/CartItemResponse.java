package com.fluxmall.cart.dto.response;

import com.fluxmall.cart.domain.CartItem;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        Integer price,
        Integer quantity,
        Integer stockQuantity,
        Integer totalPrice
) {
    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getProductPrice(),
                cartItem.getQuantity(),
                cartItem.getStockQuantity(),
                cartItem.getTotalPrice()
        );
    }
}
