package com.fluxmall.service;


import com.fluxmall.domain.entity.RecentView;
import com.fluxmall.mapper.RecentViewMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecentViewService {

    private final RecentViewMapper recentViewMapper;

    public List<RecentView> findRecentViews(Long memberId) {
        return null;
    }

    public void recordView(Long memberId, Long productId) {
    }
}