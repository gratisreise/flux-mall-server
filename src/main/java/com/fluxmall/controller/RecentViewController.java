package com.fluxmall.controller;


import com.fluxmall.service.RecentViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recent-views")
@RequiredArgsConstructor
public class RecentViewController {

    private final RecentViewService recentViewService;
}