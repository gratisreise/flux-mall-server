package com.fluxmall.review.controller;

import com.fluxmall.review.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "상품 리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;
}
