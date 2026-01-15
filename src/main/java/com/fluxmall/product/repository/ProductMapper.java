package com.fluxmall.product.repository;


import com.fluxmall.product.domain.Product;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

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