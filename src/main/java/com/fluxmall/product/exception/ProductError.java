package com.fluxmall.product.exception;

import com.fluxmall.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductError implements ErrorCode {

    // 조회 관련 에러 (PD0XX)
    NOT_FOUND("PD001", "상품을 찾을 수 없습니다."),

    // 권한 관련 에러 (PD01X)
    NOT_OWNER("PD010", "해당 상품에 대한 권한이 없습니다."),

    // 재고 관련 에러 (PD02X)
    INSUFFICIENT_STOCK("PD020", "재고가 부족합니다."),
    INVALID_STOCK_QUANTITY("PD021", "유효하지 않은 재고 수량입니다."),

    // 상태 관련 에러 (PD03X)
    ALREADY_DISCONTINUED("PD030", "이미 판매 중단된 상품입니다."),
    INVALID_STATUS("PD031", "유효하지 않은 상품 상태입니다.");

    private final String code;
    private final String message;
}
