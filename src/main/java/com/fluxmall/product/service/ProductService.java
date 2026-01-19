package com.fluxmall.product.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.dto.request.ProductCreateRequest;
import com.fluxmall.product.dto.request.ProductUpdateRequest;
import com.fluxmall.product.dto.response.ProductResponse;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    /**
     * 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductDetail(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }
        return ProductResponse.from(product);
    }

    /**
     * 상품 목록 조회 (페이징, 필터링, 정렬)
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(String category, Integer minPrice, Integer maxPrice,
                                              String sort, int page, int size) {
        int offset = page * size;
        List<Product> products = productMapper.findAllWithFilters(category, minPrice, maxPrice, sort, offset, size);
        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * 상품 목록 전체 개수 조회 (필터링)
     */
    @Transactional(readOnly = true)
    public int getProductCount(String category, Integer minPrice, Integer maxPrice) {
        return productMapper.countWithFilters(category, minPrice, maxPrice);
    }

    /**
     * 상품 키워드 검색 (페이징)
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword, int page, int size) {
        int offset = page * size;
        List<Product> products = productMapper.searchByKeywordWithPaging(keyword, offset, size);
        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * 상품 키워드 검색 결과 개수
     */
    @Transactional(readOnly = true)
    public int getSearchCount(String keyword) {
        return productMapper.countByKeyword(keyword);
    }

    /**
     * 상품 등록 (SELLER)
     */
    @Transactional
    public ProductResponse createProduct(Long memberId, ProductCreateRequest request) {
        Product product = request.toEntity(memberId);
        productMapper.insert(product);
        return ProductResponse.from(product);
    }

    /**
     * 상품 수정 (SELLER - 본인 상품만)
     */
    @Transactional
    public ProductResponse updateProduct(Long memberId, Long productId, ProductUpdateRequest request) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }
        if (!product.getMemberId().equals(memberId)) {
            throw new BusinessException(ProductError.NOT_OWNER);
        }
        if (product.getProductStatus() == Product.ProductStatus.DISCONTINUED) {
            throw new BusinessException(ProductError.ALREADY_DISCONTINUED);
        }

        // 변경할 필드만 업데이트 (null이 아닌 필드만)
        Product updatedProduct = Product.builder()
                .id(productId)
                .memberId(memberId)
                .productName(request.productName() != null ? request.productName() : product.getProductName())
                .description(request.description() != null ? request.description() : product.getDescription())
                .category(request.category() != null ? request.category() : product.getCategory())
                .price(request.price() != null ? request.price() : product.getPrice())
                .stockQuantity(request.stockQuantity() != null ? request.stockQuantity() : product.getStockQuantity())
                .productStatus(product.getProductStatus())
                .build();

        // 재고가 0이면 자동 품절 처리
        if (updatedProduct.getStockQuantity() <= 0) {
            updatedProduct = Product.builder()
                    .id(productId)
                    .memberId(memberId)
                    .productName(updatedProduct.getProductName())
                    .description(updatedProduct.getDescription())
                    .category(updatedProduct.getCategory())
                    .price(updatedProduct.getPrice())
                    .stockQuantity(updatedProduct.getStockQuantity())
                    .productStatus(Product.ProductStatus.SOLD_OUT)
                    .build();
        }

        productMapper.update(updatedProduct);
        return ProductResponse.from(productMapper.findById(productId));
    }

    /**
     * 상품 상태 변경 (SELLER - 본인 상품만)
     */
    @Transactional
    public ProductResponse updateProductStatus(Long memberId, Long productId, Product.ProductStatus newStatus) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }
        if (!product.getMemberId().equals(memberId)) {
            throw new BusinessException(ProductError.NOT_OWNER);
        }
        if (product.getProductStatus() == Product.ProductStatus.DISCONTINUED) {
            throw new BusinessException(ProductError.ALREADY_DISCONTINUED);
        }

        productMapper.updateStatus(productId, newStatus.name());
        return ProductResponse.from(productMapper.findById(productId));
    }

    /**
     * 상품 삭제 - Soft Delete (SELLER - 본인 상품만)
     */
    @Transactional
    public void deleteProduct(Long memberId, Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }
        if (!product.getMemberId().equals(memberId)) {
            throw new BusinessException(ProductError.NOT_OWNER);
        }
        if (product.getProductStatus() == Product.ProductStatus.DISCONTINUED) {
            throw new BusinessException(ProductError.ALREADY_DISCONTINUED);
        }

        productMapper.softDelete(productId);
    }

    /**
     * 내 상품 목록 조회 (SELLER)
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getMyProducts(Long memberId) {
        List<Product> products = productMapper.findByMemberId(memberId);
        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * 상품 강제 삭제 - Hard Delete (ADMIN)
     */
    @Transactional
    public void forceDeleteProduct(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }
        productMapper.delete(productId);
    }
}
