package com.fluxmall.product.repository;

import com.fluxmall.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product findById(Long id);

    Product findByIdForUpdate(Long id);

    List<Product> findAll();

    List<Product> findByMemberId(Long memberId);

    List<Product> findByCategory(String category);

    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 페이징 및 필터링이 적용된 상품 목록 조회
     */
    List<Product> findAllWithFilters(
            @Param("category") String category,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 필터 조건에 맞는 전체 상품 수 조회
     */
    int countWithFilters(
            @Param("category") String category,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice
    );

    /**
     * 키워드 검색 (페이징)
     */
    List<Product> searchByKeywordWithPaging(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 키워드 검색 결과 수
     */
    int countByKeyword(@Param("keyword") String keyword);

    void insert(Product product);

    void update(Product product);

    void updateStock(@Param("id") Long id, @Param("stockQuantity") int stockQuantity);

    void updateStatus(@Param("id") Long id, @Param("productStatus") String productStatus);

    /**
     * Soft Delete (DISCONTINUED 상태로 변경)
     */
    void softDelete(Long id);

    void delete(Long id);
}
