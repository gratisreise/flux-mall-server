package com.fluxmall.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Long memberId;
    private String orderNumber;
    private Integer totalPrice;
    private OrderStatus orderStatus;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItem> orderItems;

    public enum OrderStatus {
        PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void pay() {
        this.orderStatus = OrderStatus.PAID;
    }

    public void ship() {
        this.orderStatus = OrderStatus.SHIPPED;
    }

    public void deliver() {
        this.orderStatus = OrderStatus.DELIVERED;
    }
}
