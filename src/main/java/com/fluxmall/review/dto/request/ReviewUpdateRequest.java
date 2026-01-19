package com.fluxmall.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewUpdateRequest(
        @NotNull(message = "평점은 필수입니다")
        @Min(value = 1, message = "평점은 1 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5 이하여야 합니다")
        Integer rating,

        @NotBlank(message = "리뷰 내용은 필수입니다")
        @Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다")
        String content,

        @Size(max = 5, message = "이미지는 최대 5장까지 첨부할 수 있습니다")
        List<String> imageUrls
) {
}
