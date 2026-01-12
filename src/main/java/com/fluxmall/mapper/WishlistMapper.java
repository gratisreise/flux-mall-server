package com.fluxmall.mapper;


import com.fluxmall.domain.entity.Wishlist;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WishlistMapper {

    boolean existsByMemberIdAndProductId(Long memberId, Long productId);

    List<Wishlist> findAllByMemberId(Long memberId /* + Paging/Sort */);

    void save(Wishlist wishlist);

    void deleteByMemberIdAndProductId(Long memberId, Long productId);

    void deleteSelected(Long memberId, List<Long> productIds);
}