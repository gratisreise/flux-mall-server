package com.fluxmall.mapper;

import com.fluxmall.domain.review.entity.Review;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewMapper {

    Review findById(Long id);

    List<Review> findAllByProductId(Long productId /* + Filter/Sort/Paging */);

    Double calculateAverageRating(Long productId);

    void save(Review review);

    void update(Review review);

    void softDelete(Long id);
}