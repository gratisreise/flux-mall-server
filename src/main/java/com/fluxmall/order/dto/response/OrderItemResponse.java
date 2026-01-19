package com.fluxmall.order.dto.response;

import com.fluxmall.order.domain.OrderItem;

public record OrderItemResponse(
        Long orderItemId,
        Long productId,
        String productName,
        Integer price,
        Integer quantity,
        Integer totalPrice
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getPrice(),
                orderItem.getQuantity(),
                orderItem.getTotalPrice()
        );
    }
}
