package com.fluxmall.recentview.controller;

import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.response.ListResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.recentview.dto.request.RecentViewRecordRequest;
import com.fluxmall.recentview.dto.response.RecentViewResponse;
import com.fluxmall.recentview.service.RecentViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recent-views")
@RequiredArgsConstructor
@Tag(name = "RecentView", description = "최근 본 상품 API")
public class RecentViewController {

    private final RecentViewService recentViewService;

    @GetMapping
    @Operation(summary = "최근 본 상품 조회", description = "최근 본 상품 목록을 조회합니다. (최대 50개)")
    public ListResult<RecentViewResponse> getRecentViews(
            @CurrentMemberId Long memberId,
            @Parameter(description = "조회 개수 (기본 20, 최대 50)") @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseService.getListResult(recentViewService.getRecentViews(memberId, limit));
    }

    @PostMapping
    @Operation(summary = "최근 본 상품 기록", description = "상품 조회 기록을 저장합니다. 이미 있으면 시간만 갱신됩니다.")
    public CommonResult recordView(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody RecentViewRecordRequest request
    ) {
        recentViewService.recordView(memberId, request.productId());
        return ResponseService.getSuccessResult();
    }
}
