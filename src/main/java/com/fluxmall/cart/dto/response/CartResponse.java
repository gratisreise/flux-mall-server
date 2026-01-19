package com.fluxmall.cart.dto.response;

import com.fluxmall.cart.domain.Cart;
import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        Integer totalPrice,
        Integer itemCount
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems() == null
                ? List.of()
                : cart.getCartItems().stream()
                        .map(CartItemResponse::from)
                        .toList();

        return new CartResponse(
                cart.getId(),
                items,
                cart.getTotalPrice(),
                items.size()
        );
    }
}
