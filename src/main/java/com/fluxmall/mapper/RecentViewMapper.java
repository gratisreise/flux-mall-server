package com.fluxmall.mapper;

import com.fluxmall.domain.recentview.entity.RecentView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RecentViewMapper {

    List<RecentView> findAllByMemberIdOrderByViewedAtDesc(Long memberId);

    void saveOrUpdate(RecentView recentView);

    void deleteOldestOverLimit(Long memberId, int limit); // 예: 50개 초과 시 오래된 것 삭제
}