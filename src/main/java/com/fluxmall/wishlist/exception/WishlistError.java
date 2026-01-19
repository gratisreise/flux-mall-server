package com.fluxmall.wishlist.exception;

import com.fluxmall.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WishlistError implements ErrorCode {

    NOT_FOUND("WL001", "위시리스트 항목을 찾을 수 없습니다."),
    ALREADY_EXISTS("WL010", "이미 위시리스트에 추가된 상품입니다."),
    PRODUCT_NOT_AVAILABLE("WL020", "해당 상품은 위시리스트에 추가할 수 없습니다.");

    private final String code;
    private final String message;
}
