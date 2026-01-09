package com.fluxmall.service;



import com.fluxmall.mapper.OrderMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;

    @Transactional
    public void createOrder(Order order, List<OrderItem> orderItems) {
    }

    public Order findById(Long id) {
        return null;
    }

    public List<Order> findAllByMemberId(Long memberId) {
        return null;
    }

    @Transactional
    public void cancelOrder(Long orderId) {
    }
}