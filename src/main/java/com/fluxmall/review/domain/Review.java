package com.fluxmall.review.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long id;
    private Long productId;
    private Long memberId;
    private Long orderItemId;
    private Integer rating;
    private String content;
    private List<String> imageUrls;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조회 시 회원 정보 포함을 위한 필드
    private String memberNickname;

    public void softDelete() {
        this.isDeleted = true;
    }
}
