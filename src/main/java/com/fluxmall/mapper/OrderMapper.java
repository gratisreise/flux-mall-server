package com.fluxmall.mapper;

import com.fluxmall.domain.order.entity.Order;
import com.fluxmall.domain.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {

    Order findById(Long id);

    List<Order> findAllByMemberId(Long memberId /* + Paging */);

    void saveOrder(Order order);

    void saveOrderItem(OrderItem orderItem);

    void updateOrderStatus(Long orderId, String status);

    void cancelOrder(Long orderId);
}