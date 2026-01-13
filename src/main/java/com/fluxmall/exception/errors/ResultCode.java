package com.fluxmall.exception.errors;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultCode implements ErrorCode{

    FAILED("-1", "처리되지 못한 에러"),
    SUCCESS("1", "성공")
    ;
    private final String code;
    private final String message;
}
