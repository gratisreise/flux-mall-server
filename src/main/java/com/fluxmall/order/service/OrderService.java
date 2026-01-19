package com.fluxmall.order.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.order.domain.Order;
import com.fluxmall.order.domain.Order.OrderStatus;
import com.fluxmall.order.domain.OrderItem;
import com.fluxmall.order.dto.request.OrderCreateRequest;
import com.fluxmall.order.dto.request.OrderItemCreateRequest;
import com.fluxmall.order.dto.response.OrderListResponse;
import com.fluxmall.order.dto.response.OrderResponse;
import com.fluxmall.order.exception.OrderError;
import com.fluxmall.order.repository.OrderMapper;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Transactional
    public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(OrderError.EMPTY_ORDER_ITEMS);
        }

        // 상품 검증 및 주문 상품 목록 생성
        List<OrderItem> orderItems = new ArrayList<>();
        int totalPrice = 0;

        for (OrderItemCreateRequest itemRequest : request.items()) {
            Product product = productMapper.findById(itemRequest.productId());
            if (product == null) {
                throw new BusinessException(ProductError.NOT_FOUND);
            }
            if (product.getProductStatus() != Product.ProductStatus.ON_SALE) {
                throw new BusinessException(OrderError.CANNOT_PAY);
            }
            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new BusinessException(OrderError.INSUFFICIENT_STOCK);
            }

            int itemTotalPrice = product.getPrice() * itemRequest.quantity();
            totalPrice += itemTotalPrice;

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .quantity(itemRequest.quantity())
                    .build();
            orderItems.add(orderItem);
        }

        // 주문 생성
        String orderNumber = generateOrderNumber();
        Order order = Order.builder()
                .memberId(memberId)
                .orderNumber(orderNumber)
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PENDING)
                .shippingAddress(request.shippingAddress())
                .build();

        orderMapper.insertOrder(order);

        // 주문 상품 저장
        for (OrderItem item : orderItems) {
            OrderItem orderItemWithOrderId = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .build();
            orderMapper.insertOrderItem(orderItemWithOrderId);
        }

        // 저장된 주문 조회하여 반환
        Order savedOrder = orderMapper.findById(order.getId());
        List<OrderItem> savedOrderItems = orderMapper.findOrderItemsByOrderId(order.getId());

        Order orderWithItems = Order.builder()
                .id(savedOrder.getId())
                .memberId(savedOrder.getMemberId())
                .orderNumber(savedOrder.getOrderNumber())
                .totalPrice(savedOrder.getTotalPrice())
                .orderStatus(savedOrder.getOrderStatus())
                .shippingAddress(savedOrder.getShippingAddress())
                .createdAt(savedOrder.getCreatedAt())
                .updatedAt(savedOrder.getUpdatedAt())
                .orderItems(savedOrderItems)
                .build();

        return OrderResponse.from(orderWithItems);
    }

    @Transactional(readOnly = true)
    public List<OrderListResponse> findAllByMemberId(Long memberId, int page, int size) {
        int offset = page * size;
        List<Order> orders = orderMapper.findByMemberIdWithPaging(memberId, offset, size);

        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderMapper.findOrderItemsByOrderId(order.getId());
                    return OrderListResponse.from(order, items.size());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long memberId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(OrderError.NOT_FOUND);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(OrderError.NOT_ORDER_OWNER);
        }

        List<OrderItem> orderItems = orderMapper.findOrderItemsByOrderId(orderId);

        Order orderWithItems = Order.builder()
                .id(order.getId())
                .memberId(order.getMemberId())
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItems)
                .build();

        return OrderResponse.from(orderWithItems);
    }

    @Transactional
    public void payOrder(Long memberId, Long orderId) {
        // 비관적 락으로 주문 조회
        Order order = orderMapper.findByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException(OrderError.NOT_FOUND);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(OrderError.NOT_ORDER_OWNER);
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            if (order.getOrderStatus() == OrderStatus.PAID) {
                throw new BusinessException(OrderError.ALREADY_PAID);
            }
            throw new BusinessException(OrderError.CANNOT_PAY);
        }

        // 주문 상품별 재고 차감
        List<OrderItem> orderItems = orderMapper.findOrderItemsByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Product product = productMapper.findByIdForUpdate(item.getProductId());
            if (product == null) {
                throw new BusinessException(OrderError.CANNOT_PAY);
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(OrderError.INSUFFICIENT_STOCK);
            }
            int newStock = product.getStockQuantity() - item.getQuantity();
            productMapper.updateStock(product.getId(), newStock);
        }

        // 주문 상태 변경
        orderMapper.updateStatus(orderId, OrderStatus.PAID.name());
    }

    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {
        // 비관적 락으로 주문 조회
        Order order = orderMapper.findByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException(OrderError.NOT_FOUND);
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(OrderError.NOT_ORDER_OWNER);
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(OrderError.ALREADY_CANCELLED);
        }
        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException(OrderError.CANNOT_CANCEL);
        }

        // 결제 완료 상태였다면 재고 복원
        if (order.getOrderStatus() == OrderStatus.PAID) {
            List<OrderItem> orderItems = orderMapper.findOrderItemsByOrderId(orderId);
            for (OrderItem item : orderItems) {
                Product product = productMapper.findByIdForUpdate(item.getProductId());
                if (product != null) {
                    int newStock = product.getStockQuantity() + item.getQuantity();
                    productMapper.updateStock(product.getId(), newStock);
                }
            }
        }

        // 주문 상태 변경
        orderMapper.updateStatus(orderId, OrderStatus.CANCELLED.name());
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
