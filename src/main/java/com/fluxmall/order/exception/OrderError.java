package com.fluxmall.order.exception;

import com.fluxmall.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderError implements ErrorCode {

    // 조회 관련 에러 (OD0XX)
    NOT_FOUND("OD001", "주문을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND("OD002", "주문 상품을 찾을 수 없습니다."),

    // 권한 관련 에러 (OD01X)
    NOT_ORDER_OWNER("OD010", "해당 주문에 대한 권한이 없습니다."),

    // 상태 관련 에러 (OD02X)
    INVALID_ORDER_STATUS("OD020", "유효하지 않은 주문 상태입니다."),
    ALREADY_PAID("OD021", "이미 결제된 주문입니다."),
    ALREADY_CANCELLED("OD022", "이미 취소된 주문입니다."),
    CANNOT_CANCEL("OD023", "취소할 수 없는 주문입니다."),
    CANNOT_PAY("OD024", "결제할 수 없는 주문입니다."),

    // 재고 관련 에러 (OD03X)
    INSUFFICIENT_STOCK("OD030", "재고가 부족합니다."),

    // 주문 생성 관련 에러 (OD04X)
    EMPTY_ORDER_ITEMS("OD040", "주문할 상품이 없습니다."),
    INVALID_SHIPPING_ADDRESS("OD041", "배송지 정보가 유효하지 않습니다.");

    private final String code;
    private final String message;
}
