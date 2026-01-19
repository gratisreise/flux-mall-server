package com.fluxmall.address.controller;

import com.fluxmall.address.dto.request.AddressCreateRequest;
import com.fluxmall.address.dto.request.AddressUpdateRequest;
import com.fluxmall.address.dto.response.AddressResponse;
import com.fluxmall.address.service.ShippingAddressService;
import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.response.ListResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/me/addresses")
@RequiredArgsConstructor
@Tag(name = "Shipping Address", description = "배송지 관리 API")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    @GetMapping
    @Operation(summary = "배송지 목록 조회", description = "내 배송지 목록을 조회합니다. 기본 배송지가 가장 먼저 표시됩니다.")
    public ListResult<AddressResponse> getAddresses(@CurrentMemberId Long memberId) {
        return ResponseService.getListResult(shippingAddressService.getAddresses(memberId));
    }

    @PostMapping
    @Operation(summary = "배송지 등록", description = "새 배송지를 등록합니다. 첫 배송지는 자동으로 기본 배송지로 설정됩니다.")
    public SingleResult<AddressResponse> createAddress(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        return ResponseService.getSingleResult(shippingAddressService.createAddress(memberId, request));
    }

    @PatchMapping("/{addressId}")
    @Operation(summary = "배송지 수정", description = "배송지 정보를 수정합니다.")
    public SingleResult<AddressResponse> updateAddress(
            @CurrentMemberId Long memberId,
            @Parameter(description = "배송지 ID", required = true) @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest request
    ) {
        return ResponseService.getSingleResult(shippingAddressService.updateAddress(memberId, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "배송지 삭제", description = "배송지를 삭제합니다. 기본 배송지 삭제 시 다른 배송지가 자동으로 기본 배송지로 설정됩니다.")
    public CommonResult deleteAddress(
            @CurrentMemberId Long memberId,
            @Parameter(description = "배송지 ID", required = true) @PathVariable Long addressId
    ) {
        shippingAddressService.deleteAddress(memberId, addressId);
        return ResponseService.getSuccessResult();
    }

    @PatchMapping("/{addressId}/default")
    @Operation(summary = "기본 배송지 설정", description = "해당 배송지를 기본 배송지로 설정합니다.")
    public SingleResult<AddressResponse> setDefaultAddress(
            @CurrentMemberId Long memberId,
            @Parameter(description = "배송지 ID", required = true) @PathVariable Long addressId
    ) {
        return ResponseService.getSingleResult(shippingAddressService.setDefaultAddress(memberId, addressId));
    }
}
