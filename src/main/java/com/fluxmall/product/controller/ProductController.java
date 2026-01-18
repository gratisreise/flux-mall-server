package com.fluxmall.product.controller;

import com.fluxmall.global.response.ListResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import com.fluxmall.product.dto.response.ProductResponse;
import com.fluxmall.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "상품 API (공개)")
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회 (페이징, 필터링, 정렬)
     */
    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "상품 목록을 페이징, 필터링, 정렬하여 조회합니다.")
    public ListResult<ProductResponse> getProducts(
            @Parameter(description = "카테고리") @RequestParam(required = false) String category,
            @Parameter(description = "최소 가격") @RequestParam(required = false) Integer minPrice,
            @Parameter(description = "최대 가격") @RequestParam(required = false) Integer maxPrice,
            @Parameter(description = "정렬 (latest, priceAsc, priceDesc)") @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseService.getListResult(
                productService.getProducts(category, minPrice, maxPrice, sort, page, size)
        );
    }

    /**
     * 상품 키워드 검색
     */
    @GetMapping("/search")
    @Operation(summary = "상품 검색", description = "키워드로 상품을 검색합니다.")
    public ListResult<ProductResponse> searchProducts(
            @Parameter(description = "검색 키워드", required = true) @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseService.getListResult(
                productService.searchProducts(keyword, page, size)
        );
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보를 조회합니다.")
    public SingleResult<ProductResponse> getProduct(
            @Parameter(description = "상품 ID", required = true) @PathVariable Long productId
    ) {
        return ResponseService.getSingleResult(productService.getProductDetail(productId));
    }
}
