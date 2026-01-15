package com.fluxmall.review.repository;


import com.fluxmall.review.domain.Review;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper {

    Review findById(Long id);

    List<Review> findAllByProductId(Long productId /* + Filter/Sort/Paging */);

    Double calculateAverageRating(Long productId);

    void save(Review review);

    void update(Review review);

    void softDelete(Long id);
}