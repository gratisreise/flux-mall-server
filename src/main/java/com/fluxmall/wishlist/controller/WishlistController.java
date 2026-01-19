package com.fluxmall.wishlist.controller;

import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.response.ListResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import com.fluxmall.wishlist.dto.request.WishlistToggleRequest;
import com.fluxmall.wishlist.dto.response.WishlistResponse;
import com.fluxmall.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "위시리스트 API")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "위시리스트 조회", description = "내 위시리스트 목록을 조회합니다.")
    public ListResult<WishlistResponse> getWishlists(
            @CurrentMemberId Long memberId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseService.getListResult(wishlistService.getWishlists(memberId, page, size));
    }

    @PostMapping
    @Operation(summary = "위시리스트 토글", description = "상품을 위시리스트에 추가하거나 제거합니다. 이미 있으면 제거, 없으면 추가됩니다.")
    public SingleResult<Boolean> toggleWishlist(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody WishlistToggleRequest request
    ) {
        boolean added = wishlistService.toggleWishlist(memberId, request.productId());
        return ResponseService.getSingleResult(added);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "위시리스트 삭제", description = "상품을 위시리스트에서 명시적으로 제거합니다.")
    public CommonResult removeWishlist(
            @CurrentMemberId Long memberId,
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId
    ) {
        wishlistService.removeWishlist(memberId, productId);
        return ResponseService.getSuccessResult();
    }
}
