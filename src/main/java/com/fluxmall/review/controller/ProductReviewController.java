package com.fluxmall.review.controller;

import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.response.ListResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import com.fluxmall.review.dto.request.ReviewCreateRequest;
import com.fluxmall.review.dto.response.ReviewResponse;
import com.fluxmall.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Product Review", description = "상품 리뷰 API")
public class ProductReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "상품 리뷰 목록 조회", description = "상품의 리뷰 목록을 조회합니다. (공개)")
    public ListResult<ReviewResponse> getReviews(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
            @Parameter(description = "사진 있는 리뷰만") @RequestParam(required = false) Boolean hasImage,
            @Parameter(description = "정렬 (latest, ratingDesc, ratingAsc)") @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseService.getListResult(reviewService.getReviewsByProductId(productId, hasImage, sort, page, size));
    }

    @PostMapping
    @Operation(summary = "리뷰 작성", description = "상품 리뷰를 작성합니다. 배송 완료된 주문만 작성 가능합니다.")
    public SingleResult<ReviewResponse> createReview(
            @CurrentMemberId Long memberId,
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return ResponseService.getSingleResult(reviewService.createReview(memberId, productId, request));
    }
}
