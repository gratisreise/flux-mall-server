package com.fluxmall.exception.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthError implements ErrorCode {

    INVALID_TOKEN("AU001", "유효하지 않은 토큰입니다."),
    DUPLICATE_USERNAME("AU002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME("AU003", "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS("AU004", "이메일 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_NOT_FOUND("AU005", "회원을 찾을 수 없습니다."),
    UNAUTHORIZED("AU006", "인증이 필요합니다."),
    FORBIDDEN("AU007", "권한이 없습니다.");

    private final String code;
    private final String message;
}
