package com.fluxmall.cart.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long id;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조회 시 상품 정보 포함을 위한 필드
    private String productName;
    private Integer productPrice;
    private Integer stockQuantity;

    public int getTotalPrice() {
        return productPrice != null ? productPrice * quantity : 0;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
