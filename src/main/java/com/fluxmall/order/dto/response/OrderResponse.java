package com.fluxmall.order.dto.response;

import com.fluxmall.order.domain.Order;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String orderNumber,
        String orderStatus,
        Integer totalPrice,
        String shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems() == null
                ? List.of()
                : order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus().name(),
                order.getTotalPrice(),
                order.getShippingAddress(),
                items,
                order.getCreatedAt()
        );
    }
}
