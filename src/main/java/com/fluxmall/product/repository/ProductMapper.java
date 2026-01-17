package com.fluxmall.product.repository;

import com.fluxmall.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product findById(Long id);

    List<Product> findAll();

    List<Product> findByMemberId(Long memberId);

    List<Product> findByCategory(String category);

    List<Product> searchByKeyword(@Param("keyword") String keyword);

    void insert(Product product);

    void update(Product product);

    void updateStock(@Param("id") Long id, @Param("stockQuantity") int stockQuantity);

    void updateStatus(@Param("id") Long id, @Param("productStatus") String productStatus);

    void delete(Long id);
}
