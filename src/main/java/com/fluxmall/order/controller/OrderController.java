package com.fluxmall.order.controller;

import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.response.ListResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import com.fluxmall.order.dto.request.OrderCreateRequest;
import com.fluxmall.order.dto.response.OrderListResponse;
import com.fluxmall.order.dto.response.OrderResponse;
import com.fluxmall.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    public SingleResult<OrderResponse> createOrder(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return ResponseService.getSingleResult(orderService.createOrder(memberId, request));
    }

    @GetMapping
    @Operation(summary = "주문 목록 조회", description = "내 주문 목록을 조회합니다.")
    public ListResult<OrderListResponse> getOrders(
            @CurrentMemberId Long memberId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseService.getListResult(orderService.findAllByMemberId(memberId, page, size));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다.")
    public SingleResult<OrderResponse> getOrder(
            @CurrentMemberId Long memberId,
            @Parameter(description = "주문 ID", required = true) @PathVariable Long orderId
    ) {
        return ResponseService.getSingleResult(orderService.findById(memberId, orderId));
    }
}
