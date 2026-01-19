package com.fluxmall.admin.controller;

import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Product", description = "관리자 상품 관리 API")
public class AdminProductController {

    private final ProductService productService;

    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 강제 삭제", description = "상품을 데이터베이스에서 완전히 삭제합니다. (Hard Delete)")
    public CommonResult forceDeleteProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId
    ) {
        productService.forceDeleteProduct(productId);
        return ResponseService.getSuccessResult();
    }
}
