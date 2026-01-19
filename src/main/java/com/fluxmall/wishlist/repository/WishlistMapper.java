package com.fluxmall.wishlist.repository;

import com.fluxmall.wishlist.domain.Wishlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WishlistMapper {

    Wishlist findById(Long id);

    Wishlist findByMemberIdAndProductId(@Param("memberId") Long memberId, @Param("productId") Long productId);

    List<Wishlist> findByMemberId(Long memberId);

    List<Wishlist> findByMemberIdWithPaging(
            @Param("memberId") Long memberId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    int countByMemberId(Long memberId);

    boolean existsByMemberIdAndProductId(@Param("memberId") Long memberId, @Param("productId") Long productId);

    void insert(Wishlist wishlist);

    void delete(Long id);

    void deleteByMemberIdAndProductId(@Param("memberId") Long memberId, @Param("productId") Long productId);

    void deleteByMemberId(Long memberId);
}
