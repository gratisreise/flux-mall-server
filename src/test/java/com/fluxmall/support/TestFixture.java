package com.fluxmall.support;

import com.fluxmall.address.domain.ShippingAddress;
import com.fluxmall.cart.domain.Cart;
import com.fluxmall.cart.domain.CartItem;
import com.fluxmall.member.domain.Member;
import com.fluxmall.order.domain.Order;
import com.fluxmall.order.domain.OrderItem;
import com.fluxmall.product.domain.Product;
import com.fluxmall.review.domain.Review;
import com.fluxmall.wishlist.domain.Wishlist;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 테스트용 공통 픽스처 클래스
 * 각 도메인 엔티티의 기본 테스트 데이터를 생성합니다.
 */
public class TestFixture {

    // ==================== Member ====================

    public static Member createMember() {
        return createMember(1L, "testuser");
    }

    public static Member createMember(Long id, String username) {
        return Member.builder()
                .id(id)
                .username(username)
                .password("encoded_password")
                .nickname("테스트유저" + id)
                .role(Member.MemberRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Member createSeller() {
        return Member.builder()
                .id(2L)
                .username("seller")
                .password("encoded_password")
                .nickname("판매자")
                .role(Member.MemberRole.SELLER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Member createAdmin() {
        return Member.builder()
                .id(3L)
                .username("admin")
                .password("encoded_password")
                .nickname("관리자")
                .role(Member.MemberRole.ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Product ====================

    public static Product createProduct() {
        return createProduct(1L, "테스트 상품", 10000, 100);
    }

    public static Product createProduct(Long id, String name, int price, int stock) {
        return Product.builder()
                .id(id)
                .memberId(2L)  // 판매자 ID
                .productName(name)
                .description("상품 설명")
                .category("ELECTRONICS")
                .price(price)
                .stockQuantity(stock)
                .productStatus(Product.ProductStatus.ON_SALE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Product createSoldOutProduct() {
        return Product.builder()
                .id(2L)
                .memberId(2L)
                .productName("품절 상품")
                .description("품절된 상품입니다")
                .category("ELECTRONICS")
                .price(20000)
                .stockQuantity(0)
                .productStatus(Product.ProductStatus.SOLD_OUT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Product createDiscontinuedProduct() {
        return Product.builder()
                .id(3L)
                .memberId(2L)
                .productName("삭제된 상품")
                .description("삭제된 상품입니다")
                .category("ELECTRONICS")
                .price(30000)
                .stockQuantity(50)
                .productStatus(Product.ProductStatus.DISCONTINUED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Order ====================

    public static Order createOrder() {
        return createOrder(1L, 1L, Order.OrderStatus.PENDING);
    }

    public static Order createOrder(Long id, Long memberId, Order.OrderStatus status) {
        return Order.builder()
                .id(id)
                .memberId(memberId)
                .orderNumber("ORD-" + System.currentTimeMillis())
                .totalPrice(30000)
                .orderStatus(status)
                .shippingAddress("서울시 강남구 테스트로 123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Order createPaidOrder(Long id, Long memberId) {
        return createOrder(id, memberId, Order.OrderStatus.PAID);
    }

    public static Order createDeliveredOrder(Long id, Long memberId) {
        return createOrder(id, memberId, Order.OrderStatus.DELIVERED);
    }

    public static Order createCancelledOrder(Long id, Long memberId) {
        return createOrder(id, memberId, Order.OrderStatus.CANCELLED);
    }

    // ==================== OrderItem ====================

    public static OrderItem createOrderItem() {
        return createOrderItem(1L, 1L, 1L);
    }

    public static OrderItem createOrderItem(Long id, Long orderId, Long productId) {
        return OrderItem.builder()
                .id(id)
                .orderId(orderId)
                .productId(productId)
                .productName("테스트 상품")
                .price(10000)
                .quantity(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static List<OrderItem> createOrderItems(Long orderId) {
        return List.of(
                createOrderItem(1L, orderId, 1L),
                createOrderItem(2L, orderId, 2L)
        );
    }

    // ==================== Cart ====================

    public static Cart createCart() {
        return createCart(1L, 1L);
    }

    public static Cart createCart(Long id, Long memberId) {
        return Cart.builder()
                .id(id)
                .memberId(memberId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Cart createCartWithItems(Long id, Long memberId) {
        return Cart.builder()
                .id(id)
                .memberId(memberId)
                .cartItems(createCartItems(id))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== CartItem ====================

    public static CartItem createCartItem() {
        return createCartItem(1L, 1L, 1L, 2);
    }

    public static CartItem createCartItem(Long id, Long cartId, Long productId, int quantity) {
        return CartItem.builder()
                .id(id)
                .cartId(cartId)
                .productId(productId)
                .quantity(quantity)
                .productName("테스트 상품")
                .productPrice(10000)
                .stockQuantity(100)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static List<CartItem> createCartItems(Long cartId) {
        return List.of(
                createCartItem(1L, cartId, 1L, 2),
                createCartItem(2L, cartId, 2L, 1)
        );
    }

    // ==================== Review ====================

    public static Review createReview() {
        return createReview(1L, 1L, 1L);
    }

    public static Review createReview(Long id, Long productId, Long memberId) {
        return Review.builder()
                .id(id)
                .productId(productId)
                .memberId(memberId)
                .orderItemId(1L)
                .rating(5)
                .content("좋은 상품입니다!")
                .imageUrls(List.of("https://example.com/image1.jpg"))
                .isDeleted(false)
                .memberNickname("테스트유저")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Review createDeletedReview() {
        return Review.builder()
                .id(2L)
                .productId(1L)
                .memberId(1L)
                .orderItemId(2L)
                .rating(3)
                .content("삭제된 리뷰입니다")
                .isDeleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Wishlist ====================

    public static Wishlist createWishlist() {
        return createWishlist(1L, 1L, 1L);
    }

    public static Wishlist createWishlist(Long id, Long memberId, Long productId) {
        return Wishlist.builder()
                .id(id)
                .memberId(memberId)
                .productId(productId)
                .productName("테스트 상품")
                .productPrice(10000)
                .productStatus("ON_SALE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== ShippingAddress ====================

    public static ShippingAddress createShippingAddress() {
        return createShippingAddress(1L, 1L, true);
    }

    public static ShippingAddress createShippingAddress(Long id, Long memberId, boolean isDefault) {
        return ShippingAddress.builder()
                .id(id)
                .memberId(memberId)
                .recipientName("홍길동")
                .phone("010-1234-5678")
                .postcode("12345")
                .address1("서울시 강남구 테스트로 123")
                .address2("101동 101호")
                .isDefault(isDefault)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static List<ShippingAddress> createShippingAddresses(Long memberId) {
        return List.of(
                createShippingAddress(1L, memberId, true),
                createShippingAddress(2L, memberId, false),
                createShippingAddress(3L, memberId, false)
        );
    }
}
