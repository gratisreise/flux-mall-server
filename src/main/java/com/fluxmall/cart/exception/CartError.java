package com.fluxmall.cart.exception;

import com.fluxmall.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CartError implements ErrorCode {

    // 조회 관련 에러 (CT0XX)
    CART_NOT_FOUND("CT001", "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND("CT002", "장바구니 상품을 찾을 수 없습니다."),

    // 권한 관련 에러 (CT01X)
    NOT_CART_OWNER("CT010", "해당 장바구니에 대한 권한이 없습니다."),

    // 수량 관련 에러 (CT02X)
    INVALID_QUANTITY("CT020", "유효하지 않은 수량입니다."),
    EXCEEDS_STOCK("CT021", "재고 수량을 초과할 수 없습니다."),
    QUANTITY_MUST_BE_POSITIVE("CT022", "수량은 1개 이상이어야 합니다."),

    // 상품 관련 에러 (CT03X)
    PRODUCT_NOT_AVAILABLE("CT030", "해당 상품은 현재 구매할 수 없습니다.");

    private final String code;
    private final String message;
}
