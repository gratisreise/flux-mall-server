package com.fluxmall.product.controller;

import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.response.ListResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import com.fluxmall.product.dto.request.ProductCreateRequest;
import com.fluxmall.product.dto.request.ProductStatusUpdateRequest;
import com.fluxmall.product.dto.request.ProductUpdateRequest;
import com.fluxmall.product.dto.response.ProductResponse;
import com.fluxmall.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
@Tag(name = "Seller Product", description = "판매자 상품 관리 API")
@PreAuthorize("hasRole('SELLER')")
public class SellerProductController {

    private final ProductService productService;

    /**
     * 내 상품 목록 조회
     */
    @GetMapping
    @Operation(summary = "내 상품 목록 조회", description = "판매자 본인이 등록한 상품 목록을 조회합니다.")
    public ListResult<ProductResponse> getMyProducts(@CurrentMemberId Long memberId) {
        return ResponseService.getListResult(productService.getMyProducts(memberId));
    }

    /**
     * 상품 등록
     */
    @PostMapping
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    public SingleResult<ProductResponse> createProduct(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return ResponseService.getSingleResult(productService.createProduct(memberId, request));
    }

    /**
     * 상품 수정
     */
    @PatchMapping("/{productId}")
    @Operation(summary = "상품 수정", description = "본인이 등록한 상품을 수정합니다.")
    public SingleResult<ProductResponse> updateProduct(
            @CurrentMemberId Long memberId,
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseService.getSingleResult(productService.updateProduct(memberId, productId, request));
    }

    /**
     * 상품 상태 변경 (판매중/품절)
     */
    @PatchMapping("/{productId}/status")
    @Operation(summary = "상품 상태 변경", description = "상품의 판매 상태를 변경합니다. (ON_SALE, SOLD_OUT)")
    public SingleResult<ProductResponse> updateProductStatus(
            @CurrentMemberId Long memberId,
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        return ResponseService.getSingleResult(
                productService.updateProductStatus(memberId, productId, request.productStatus())
        );
    }

    /**
     * 상품 삭제 (Soft Delete)
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 삭제", description = "본인이 등록한 상품을 삭제합니다. (판매 중단 처리)")
    public CommonResult deleteProduct(
            @CurrentMemberId Long memberId,
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId
    ) {
        productService.deleteProduct(memberId, productId);
        return ResponseService.getSuccessResult();
    }
}
