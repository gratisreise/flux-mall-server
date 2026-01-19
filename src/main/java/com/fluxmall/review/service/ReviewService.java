package com.fluxmall.review.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.order.domain.Order;
import com.fluxmall.order.domain.OrderItem;
import com.fluxmall.order.repository.OrderMapper;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import com.fluxmall.review.domain.Review;
import com.fluxmall.review.dto.request.ReviewCreateRequest;
import com.fluxmall.review.dto.request.ReviewUpdateRequest;
import com.fluxmall.review.dto.response.ReviewResponse;
import com.fluxmall.review.exception.ReviewError;
import com.fluxmall.review.repository.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProductId(Long productId, Boolean hasImage, String sort, int page, int size) {
        // 상품 존재 여부 확인
        if (productMapper.findById(productId) == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }

        int offset = page * size;
        List<Review> reviews = reviewMapper.findByProductIdWithPaging(productId, hasImage, sort, offset, size);

        return reviews.stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public ReviewResponse createReview(Long memberId, Long productId, ReviewCreateRequest request) {
        // 상품 존재 여부 확인
        if (productMapper.findById(productId) == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }

        // 주문 상품 확인 및 배송 완료 여부 확인
        List<OrderItem> orderItems = orderMapper.findOrderItemsByOrderId(request.orderItemId());
        OrderItem orderItem = null;

        // orderItemId로 직접 찾기
        for (OrderItem item : orderMapper.findOrderItemsByOrderId(request.orderItemId())) {
            if (item.getId().equals(request.orderItemId())) {
                orderItem = item;
                break;
            }
        }

        // 더 간단한 방법: 모든 주문에서 해당 orderItem 찾기
        Order order = findOrderByOrderItemId(memberId, request.orderItemId(), productId);

        if (order == null) {
            throw new BusinessException(ReviewError.ORDER_NOT_DELIVERED);
        }

        if (order.getOrderStatus() != Order.OrderStatus.DELIVERED) {
            throw new BusinessException(ReviewError.ORDER_NOT_DELIVERED);
        }

        // 이미 해당 주문 상품에 리뷰 작성 여부 확인
        if (reviewMapper.existsByMemberIdAndOrderItemId(memberId, request.orderItemId())) {
            throw new BusinessException(ReviewError.ALREADY_REVIEWED);
        }

        // 리뷰 생성
        Review review = Review.builder()
                .productId(productId)
                .memberId(memberId)
                .orderItemId(request.orderItemId())
                .rating(request.rating())
                .content(request.content())
                .imageUrls(request.imageUrls())
                .isDeleted(false)
                .build();

        reviewMapper.insert(review);

        // 저장된 리뷰 조회하여 반환
        Review savedReview = reviewMapper.findById(review.getId());
        return ReviewResponse.from(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(Long memberId, Long reviewId, ReviewUpdateRequest request) {
        Review review = reviewMapper.findById(reviewId);
        if (review == null || review.getIsDeleted()) {
            throw new BusinessException(ReviewError.NOT_FOUND);
        }
        if (!review.getMemberId().equals(memberId)) {
            throw new BusinessException(ReviewError.NOT_REVIEW_OWNER);
        }

        Review updatedReview = Review.builder()
                .id(reviewId)
                .rating(request.rating())
                .content(request.content())
                .imageUrls(request.imageUrls())
                .build();

        reviewMapper.update(updatedReview);

        // 업데이트된 리뷰 조회하여 반환
        Review result = reviewMapper.findById(reviewId);
        return ReviewResponse.from(result);
    }

    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        Review review = reviewMapper.findById(reviewId);
        if (review == null || review.getIsDeleted()) {
            throw new BusinessException(ReviewError.NOT_FOUND);
        }
        if (!review.getMemberId().equals(memberId)) {
            throw new BusinessException(ReviewError.NOT_REVIEW_OWNER);
        }

        reviewMapper.softDelete(reviewId);
    }

    private Order findOrderByOrderItemId(Long memberId, Long orderItemId, Long productId) {
        // 회원의 모든 주문에서 해당 주문 상품 찾기
        List<Order> orders = orderMapper.findByMemberId(memberId);
        for (Order order : orders) {
            List<OrderItem> items = orderMapper.findOrderItemsByOrderId(order.getId());
            for (OrderItem item : items) {
                if (item.getId().equals(orderItemId) && item.getProductId().equals(productId)) {
                    return order;
                }
            }
        }
        return null;
    }
}
