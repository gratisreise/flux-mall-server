package com.fluxmall.mapper;


import com.fluxmall.domain.entity.Order;
import com.fluxmall.domain.entity.OrderItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {

    Order findById(Long id);

    List<Order> findAllByMemberId(Long memberId /* + Paging */);

    void saveOrder(Order order);

    void saveOrderItem(OrderItem orderItem);

    void updateOrderStatus(Long orderId, String status);

    void cancelOrder(Long orderId);
}