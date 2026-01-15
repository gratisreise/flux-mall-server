package com.fluxmall.exception.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthError implements ErrorCode {

    // Token validation errors
    INVALID_TOKEN("AU001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("AU002", "만료된 토큰입니다."),
    MALFORMED_TOKEN("AU003", "올바르지 않은 형식의 토큰입니다."),
    UNSUPPORTED_TOKEN("AU004", "지원하지 않는 토큰입니다."),
    BLACKLISTED_TOKEN("AU005", "로그아웃된 토큰입니다."),

    // Registration errors
    DUPLICATE_USERNAME("AU010", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME("AU011", "이미 사용 중인 닉네임입니다."),

    // Authentication errors
    INVALID_CREDENTIALS("AU020", "이메일 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_NOT_FOUND("AU021", "회원을 찾을 수 없습니다."),
    ACCOUNT_DISABLED("AU022", "비활성화된 계정입니다."),

    // Authorization errors
    UNAUTHORIZED("AU030", "인증이 필요합니다."),
    FORBIDDEN("AU031", "권한이 없습니다."),
    INSUFFICIENT_PERMISSIONS("AU032", "해당 작업을 수행할 권한이 없습니다.");

    private final String code;
    private final String message;
}
