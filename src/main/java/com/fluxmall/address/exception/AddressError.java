package com.fluxmall.address.exception;

import com.fluxmall.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AddressError implements ErrorCode {

    // 조회 관련 에러 (AD00X)
    NOT_FOUND("AD001", "배송지를 찾을 수 없습니다."),

    // 권한 관련 에러 (AD01X)
    NOT_OWNER("AD010", "해당 배송지에 대한 권한이 없습니다."),

    // 검증 관련 에러 (AD02X)
    CANNOT_DELETE_DEFAULT("AD020", "기본 배송지는 삭제할 수 없습니다. 다른 배송지를 기본으로 설정 후 삭제해주세요."),
    MAX_ADDRESS_EXCEEDED("AD021", "배송지는 최대 10개까지 등록할 수 있습니다.");

    private final String code;
    private final String message;
}
