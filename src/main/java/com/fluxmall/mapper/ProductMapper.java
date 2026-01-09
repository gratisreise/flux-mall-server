package com.fluxmall.mapper;

import com.fluxmall.domain.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product findById(Long id);

    List<Product> findAllWithPaging(/* Paging Parameter DTO */);

    List<Product> searchByKeyword(/* Search Parameter DTO */);

    void save(Product product);

    void update(Product product);

    void updateStock(Long productId, int quantity);

    void softDelete(Long id);
}