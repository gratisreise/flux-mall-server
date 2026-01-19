package com.fluxmall.admin.controller;

import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Review", description = "관리자 리뷰 관리 API")
public class AdminReviewController {

    private final ReviewService reviewService;

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 강제 삭제", description = "부적절한 리뷰를 데이터베이스에서 완전히 삭제합니다. (Hard Delete)")
    public CommonResult forceDeleteReview(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId
    ) {
        reviewService.forceDeleteReview(reviewId);
        return ResponseService.getSuccessResult();
    }
}
