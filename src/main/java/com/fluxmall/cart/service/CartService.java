package com.fluxmall.cart.service;

import com.fluxmall.cart.domain.Cart;
import com.fluxmall.cart.domain.CartItem;
import com.fluxmall.cart.dto.request.CartItemAddRequest;
import com.fluxmall.cart.dto.request.CartItemUpdateRequest;
import com.fluxmall.cart.dto.response.CartResponse;
import com.fluxmall.cart.exception.CartError;
import com.fluxmall.cart.repository.CartMapper;
import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        List<CartItem> cartItems = cartMapper.findCartItemsByCartId(cart.getId());

        Cart cartWithItems = Cart.builder()
                .id(cart.getId())
                .memberId(cart.getMemberId())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .cartItems(cartItems)
                .build();

        return CartResponse.from(cartWithItems);
    }

    @Transactional
    public void addItem(Long memberId, CartItemAddRequest request) {
        // 상품 존재 및 판매 가능 여부 확인
        Product product = productMapper.findById(request.productId());
        if (product == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }
        if (product.getProductStatus() != Product.ProductStatus.ON_SALE) {
            throw new BusinessException(CartError.PRODUCT_NOT_AVAILABLE);
        }

        // 장바구니 조회 또는 생성
        Cart cart = getOrCreateCart(memberId);

        // 기존 장바구니 아이템 확인
        CartItem existingItem = cartMapper.findCartItemByCartIdAndProductId(cart.getId(), request.productId());

        int totalQuantity = request.quantity();
        if (existingItem != null) {
            totalQuantity += existingItem.getQuantity();
        }

        // 재고 확인
        if (totalQuantity > product.getStockQuantity()) {
            throw new BusinessException(CartError.EXCEEDS_STOCK);
        }

        if (existingItem != null) {
            // 기존 아이템 수량 업데이트
            cartMapper.updateCartItemQuantity(existingItem.getId(), totalQuantity);
        } else {
            // 새 아이템 추가
            CartItem newItem = CartItem.builder()
                    .cartId(cart.getId())
                    .productId(request.productId())
                    .quantity(request.quantity())
                    .build();
            cartMapper.insertCartItem(newItem);
        }
    }

    @Transactional
    public void updateItemQuantity(Long memberId, Long cartItemId, CartItemUpdateRequest request) {
        CartItem cartItem = cartMapper.findCartItemById(cartItemId);
        if (cartItem == null) {
            throw new BusinessException(CartError.CART_ITEM_NOT_FOUND);
        }

        // 장바구니 소유권 확인
        Cart cart = cartMapper.findById(cartItem.getCartId());
        if (cart == null || !cart.getMemberId().equals(memberId)) {
            throw new BusinessException(CartError.NOT_CART_OWNER);
        }

        // 상품 재고 확인
        Product product = productMapper.findById(cartItem.getProductId());
        if (product == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }
        if (request.quantity() > product.getStockQuantity()) {
            throw new BusinessException(CartError.EXCEEDS_STOCK);
        }

        cartMapper.updateCartItemQuantity(cartItemId, request.quantity());
    }

    @Transactional
    public void removeItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartMapper.findCartItemById(cartItemId);
        if (cartItem == null) {
            throw new BusinessException(CartError.CART_ITEM_NOT_FOUND);
        }

        // 장바구니 소유권 확인
        Cart cart = cartMapper.findById(cartItem.getCartId());
        if (cart == null || !cart.getMemberId().equals(memberId)) {
            throw new BusinessException(CartError.NOT_CART_OWNER);
        }

        cartMapper.deleteCartItem(cartItemId);
    }

    @Transactional
    public void removeItems(Long memberId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return;
        }

        // 모든 아이템의 소유권 확인
        for (Long cartItemId : cartItemIds) {
            CartItem cartItem = cartMapper.findCartItemById(cartItemId);
            if (cartItem == null) {
                throw new BusinessException(CartError.CART_ITEM_NOT_FOUND);
            }

            Cart cart = cartMapper.findById(cartItem.getCartId());
            if (cart == null || !cart.getMemberId().equals(memberId)) {
                throw new BusinessException(CartError.NOT_CART_OWNER);
            }
        }

        cartMapper.deleteCartItemsByIds(cartItemIds);
    }

    private Cart getOrCreateCart(Long memberId) {
        Cart cart = cartMapper.findByMemberId(memberId);
        if (cart == null) {
            cart = Cart.builder()
                    .memberId(memberId)
                    .build();
            cartMapper.insertCart(cart);
        }
        return cart;
    }
}
