package com.fluxmall.cart.controller;

import com.fluxmall.cart.dto.request.CartItemAddRequest;
import com.fluxmall.cart.dto.request.CartItemRemoveRequest;
import com.fluxmall.cart.dto.request.CartItemUpdateRequest;
import com.fluxmall.cart.dto.response.CartResponse;
import com.fluxmall.cart.service.CartService;
import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "장바구니 API")
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 조회
     */
    @GetMapping
    @Operation(summary = "장바구니 조회", description = "현재 로그인한 회원의 장바구니를 조회합니다.")
    public SingleResult<CartResponse> getCart(@CurrentMemberId Long memberId) {
        return ResponseService.getSingleResult(cartService.getCart(memberId));
    }

    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/items")
    @Operation(summary = "장바구니 상품 추가", description = "장바구니에 상품을 추가합니다. 동일 상품이 있으면 수량이 합산됩니다.")
    public CommonResult addItem(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        cartService.addItem(memberId, request);
        return ResponseService.getSuccessResult();
    }

    /**
     * 장바구니 상품 수량 수정
     */
    @PatchMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 수량 수정", description = "장바구니 상품의 수량을 수정합니다.")
    public CommonResult updateItemQuantity(
            @CurrentMemberId Long memberId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        cartService.updateItemQuantity(memberId, cartItemId, request);
        return ResponseService.getSuccessResult();
    }

    /**
     * 장바구니 상품 개별 삭제
     */
    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 특정 상품을 삭제합니다.")
    public CommonResult removeItem(
            @CurrentMemberId Long memberId,
            @PathVariable Long cartItemId
    ) {
        cartService.removeItem(memberId, cartItemId);
        return ResponseService.getSuccessResult();
    }

    /**
     * 장바구니 상품 일괄 삭제
     */
    @DeleteMapping("/items")
    @Operation(summary = "장바구니 상품 일괄 삭제", description = "장바구니에서 선택한 상품들을 일괄 삭제합니다.")
    public CommonResult removeItems(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody CartItemRemoveRequest request
    ) {
        cartService.removeItems(memberId, request.cartItemIds());
        return ResponseService.getSuccessResult();
    }
}
