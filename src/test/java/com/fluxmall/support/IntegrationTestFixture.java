package com.fluxmall.support;

import com.fluxmall.address.dto.request.AddressCreateRequest;
import com.fluxmall.auth.dto.request.LoginRequest;
import com.fluxmall.cart.dto.request.CartItemAddRequest;
import com.fluxmall.cart.dto.request.CartItemUpdateRequest;
import com.fluxmall.member.dto.request.RegisterRequest;
import com.fluxmall.order.dto.request.OrderCreateRequest;
import com.fluxmall.order.dto.request.OrderItemCreateRequest;
import com.fluxmall.product.dto.request.ProductCreateRequest;
import com.fluxmall.product.dto.request.ProductUpdateRequest;
import com.fluxmall.review.dto.request.ReviewCreateRequest;
import com.fluxmall.review.dto.request.ReviewUpdateRequest;

import java.util.List;

/**
 * 통합 테스트용 Request DTO 빌더
 */
public class IntegrationTestFixture {

    // ==================== Auth ====================

    public static LoginRequest createLoginRequest(String username, String password) {
        return new LoginRequest(username, password);
    }

    // ==================== Member ====================

    public static RegisterRequest createRegisterRequest(String username, String password, String nickname) {
        return new RegisterRequest(username, password, nickname);
    }

    // ==================== Product ====================

    public static ProductCreateRequest createProductRequest() {
        return new ProductCreateRequest(
                "테스트 상품",
                "상품 설명입니다",
                "ELECTRONICS",
                10000,
                100
        );
    }

    public static ProductCreateRequest createProductRequest(String name, int price, int stock) {
        return new ProductCreateRequest(
                name,
                "상품 설명입니다",
                "ELECTRONICS",
                price,
                stock
        );
    }

    public static ProductUpdateRequest createProductUpdateRequest() {
        return new ProductUpdateRequest(
                "수정된 상품",
                "수정된 설명",
                null,
                15000,
                null
        );
    }

    // ==================== Review ====================

    public static ReviewCreateRequest createReviewRequest(Long orderItemId) {
        return new ReviewCreateRequest(
                orderItemId,
                5,
                "좋은 상품입니다!",
                List.of()
        );
    }

    public static ReviewUpdateRequest createReviewUpdateRequest() {
        return new ReviewUpdateRequest(
                4,
                "수정된 리뷰입니다",
                List.of()
        );
    }

    // ==================== Cart ====================

    public static CartItemAddRequest createCartItemAddRequest(Long productId, int quantity) {
        return new CartItemAddRequest(productId, quantity);
    }

    public static CartItemUpdateRequest createCartItemUpdateRequest(int quantity) {
        return new CartItemUpdateRequest(quantity);
    }

    // ==================== Order ====================

    public static OrderCreateRequest createOrderRequest(List<OrderItemCreateRequest> items, String shippingAddress) {
        return new OrderCreateRequest(items, shippingAddress);
    }

    public static OrderItemCreateRequest createOrderItemRequest(Long productId, int quantity) {
        return new OrderItemCreateRequest(productId, quantity);
    }

    // ==================== ShippingAddress ====================

    public static AddressCreateRequest createAddressRequest() {
        return new AddressCreateRequest(
                "홍길동",
                "010-1234-5678",
                "12345",
                "서울시 강남구 테스트로 123",
                "101동 101호",
                false
        );
    }

    public static AddressCreateRequest createDefaultAddressRequest() {
        return new AddressCreateRequest(
                "홍길동",
                "010-1234-5678",
                "12345",
                "서울시 강남구 테스트로 123",
                "101동 101호",
                true  // 기본 배송지
        );
    }
}
