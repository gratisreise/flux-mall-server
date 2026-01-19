package com.fluxmall.order.dto.response;

import com.fluxmall.order.domain.Order;
import java.time.LocalDateTime;

public record OrderListResponse(
        Long orderId,
        String orderNumber,
        String orderStatus,
        Integer totalPrice,
        Integer itemCount,
        LocalDateTime createdAt
) {
    public static OrderListResponse from(Order order, int itemCount) {
        return new OrderListResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus().name(),
                order.getTotalPrice(),
                itemCount,
                order.getCreatedAt()
        );
    }
}
