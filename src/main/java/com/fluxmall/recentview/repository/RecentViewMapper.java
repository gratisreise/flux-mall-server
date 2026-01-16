package com.fluxmall.recentview.repository;

import com.fluxmall.recentview.domain.RecentView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecentViewMapper {

    RecentView findById(Long id);

    RecentView findByMemberIdAndProductId(@Param("memberId") Long memberId, @Param("productId") Long productId);

    List<RecentView> findByMemberId(@Param("memberId") Long memberId, @Param("limit") int limit);

    int countByMemberId(Long memberId);

    void insert(RecentView recentView);

    void updateViewedAt(@Param("memberId") Long memberId, @Param("productId") Long productId);

    void delete(Long id);

    void deleteByMemberId(Long memberId);

    void deleteOldestByMemberId(@Param("memberId") Long memberId, @Param("keepCount") int keepCount);
}
