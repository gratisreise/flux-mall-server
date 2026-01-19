package com.fluxmall.review.dto.response;

import com.fluxmall.review.domain.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long reviewId,
        Long productId,
        Long memberId,
        String memberNickname,
        Integer rating,
        String content,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getMemberId(),
                review.getMemberNickname(),
                review.getRating(),
                review.getContent(),
                review.getImageUrls() != null ? review.getImageUrls() : List.of(),
                review.getCreatedAt()
        );
    }
}
