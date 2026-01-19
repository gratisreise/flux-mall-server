package com.fluxmall.recentview.service;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.product.domain.Product;
import com.fluxmall.product.exception.ProductError;
import com.fluxmall.product.repository.ProductMapper;
import com.fluxmall.recentview.domain.RecentView;
import com.fluxmall.recentview.dto.response.RecentViewResponse;
import com.fluxmall.recentview.repository.RecentViewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentViewService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_RECENT_VIEWS = 50;

    private final RecentViewMapper recentViewMapper;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<RecentViewResponse> getRecentViews(Long memberId, int limit) {
        int effectiveLimit = Math.min(limit, MAX_RECENT_VIEWS);
        List<RecentView> recentViews = recentViewMapper.findByMemberId(memberId, effectiveLimit);

        return recentViews.stream()
                .map(RecentViewResponse::from)
                .toList();
    }

    @Transactional
    public void recordView(Long memberId, Long productId) {
        // 상품 존재 여부 확인
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(ProductError.NOT_FOUND);
        }

        // 기존 기록이 있으면 시간만 갱신 (UPSERT)
        RecentView existing = recentViewMapper.findByMemberIdAndProductId(memberId, productId);

        if (existing != null) {
            recentViewMapper.updateViewedAt(memberId, productId);
        } else {
            RecentView recentView = RecentView.builder()
                    .memberId(memberId)
                    .productId(productId)
                    .build();
            recentViewMapper.insert(recentView);

            // 최대 개수 초과 시 오래된 기록 삭제
            cleanupOldViews(memberId);
        }
    }

    private void cleanupOldViews(Long memberId) {
        int count = recentViewMapper.countByMemberId(memberId);
        if (count > MAX_RECENT_VIEWS) {
            recentViewMapper.deleteOldestByMemberId(memberId, MAX_RECENT_VIEWS);
        }
    }
}
