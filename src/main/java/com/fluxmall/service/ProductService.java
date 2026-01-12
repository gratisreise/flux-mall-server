package com.fluxmall.service;


import com.fluxmall.domain.entity.Product;
import com.fluxmall.mapper.ProductMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public Product findById(Long id) {
        return null;
    }

    public List<Product> findAllWithPaging(/* Paging DTO */) {
        return null;
    }

    public List<Product> searchByKeyword(/* Search DTO */) {
        return null;
    }

    public void register(Product product) {
    }

    public void update(Product product) {
    }

    public void updateStock(Long productId, int quantity) {
    }

    public void softDelete(Long id) {
    }
}