package com.fluxmall.mapper;


import com.fluxmall.domain.entity.RecentView;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecentViewMapper {

    List<RecentView> findAllByMemberIdOrderByViewedAtDesc(Long memberId);

    void saveOrUpdate(RecentView recentView);

    void deleteOldestOverLimit(Long memberId, int limit); // 예: 50개 초과 시 오래된 것 삭제
}