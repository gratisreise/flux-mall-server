package com.fluxmall.exception.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthError implements ErrorCode{

    INVALID_TOKEN("AU001", "유효하지 않은 토큰입니다.")
    ;

    private final String  code;
    private final String message;
}
