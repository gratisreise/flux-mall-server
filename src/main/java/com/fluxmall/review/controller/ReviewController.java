package com.fluxmall.review.controller;

import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import com.fluxmall.review.dto.request.ReviewUpdateRequest;
import com.fluxmall.review.dto.response.ReviewResponse;
import com.fluxmall.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관리 API")
public class ReviewController {

    private final ReviewService reviewService;

    @PatchMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    public SingleResult<ReviewResponse> updateReview(
            @CurrentMemberId Long memberId,
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseService.getSingleResult(reviewService.updateReview(memberId, reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다. (Soft Delete)")
    public CommonResult deleteReview(
            @CurrentMemberId Long memberId,
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(memberId, reviewId);
        return ResponseService.getSuccessResult();
    }
}
