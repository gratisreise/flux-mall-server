package com.fluxmall.auth.dto.response;

public record TokenRefreshResponse(
    String accessToken,
    String message
) {
    public static TokenRefreshResponse of(String accessToken) {
        return new TokenRefreshResponse(accessToken, "새로운 Access Token이 발급되었습니다.");
    }
}
