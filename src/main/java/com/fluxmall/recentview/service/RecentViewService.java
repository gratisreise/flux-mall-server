package com.fluxmall.recentview.service;


import com.fluxmall.recentview.domain.RecentView;
import com.fluxmall.recentview.repository.RecentViewMapper;
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