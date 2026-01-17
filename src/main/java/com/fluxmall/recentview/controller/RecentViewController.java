package com.fluxmall.recentview.controller;

import com.fluxmall.recentview.service.RecentViewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recent-views")
@RequiredArgsConstructor
@Tag(name = "RecentView", description = "최근 본 상품 API")
public class RecentViewController {

    private final RecentViewService recentViewService;
}
