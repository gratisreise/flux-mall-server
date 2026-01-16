package com.fluxmall.order.repository;

import com.fluxmall.order.domain.Order;
import com.fluxmall.order.domain.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    Order findById(Long id);

    Order findByOrderNumber(String orderNumber);

    List<Order> findByMemberId(Long memberId);

    List<OrderItem> findOrderItemsByOrderId(Long orderId);

    void insertOrder(Order order);

    void insertOrderItem(OrderItem orderItem);

    void updateStatus(@Param("id") Long id, @Param("orderStatus") String orderStatus);

    void deleteOrder(Long id);

    void deleteOrderItemsByOrderId(Long orderId);
}
