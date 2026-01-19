package com.fluxmall.review.repository;

import com.fluxmall.review.domain.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    Review findById(Long id);

    List<Review> findByProductId(Long productId);

    List<Review> findByProductIdWithPaging(
            @Param("productId") Long productId,
            @Param("hasImage") Boolean hasImage,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    List<Review> findByMemberId(Long memberId);

    boolean existsByMemberIdAndProductId(@Param("memberId") Long memberId, @Param("productId") Long productId);

    boolean existsByMemberIdAndOrderItemId(@Param("memberId") Long memberId, @Param("orderItemId") Long orderItemId);

    Double calculateAverageRating(Long productId);

    int countByProductId(Long productId);

    void insert(Review review);

    void update(Review review);

    void softDelete(Long id);

    void deleteByMemberId(Long memberId);
}
