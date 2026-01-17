package com.fluxmall.wishlist.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {
    private Long id;
    private Long memberId;
    private Long productId;
    private LocalDateTime createdAt;

    // 조회 시 상품 정보 포함을 위한 필드
    private String productName;
    private Integer productPrice;
    private String productStatus;
}
