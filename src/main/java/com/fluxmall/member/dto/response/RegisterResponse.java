package com.fluxmall.member.dto.response;

public record RegisterResponse(
    Long memberId,
    String message
) {
    public static RegisterResponse of(Long memberId) {
        return new RegisterResponse(memberId, "회원가입이 완료되었습니다.");
    }
}
