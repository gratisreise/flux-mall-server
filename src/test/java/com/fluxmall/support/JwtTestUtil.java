package com.fluxmall.support;

import com.fluxmall.global.util.JwtUtil;

/**
 * JWT 테스트 유틸리티
 * - 통합 테스트에서 JWT 토큰 생성 및 인증 헤더 생성
 */
public class JwtTestUtil {

    /**
     * Access Token 생성
     */
    public static String generateAccessToken(JwtUtil jwtUtil, Long memberId, String role) {
        return jwtUtil.createAccessToken(memberId, role);
    }

    /**
     * Refresh Token 생성
     */
    public static String generateRefreshToken(JwtUtil jwtUtil, Long memberId) {
        return jwtUtil.createRefreshToken(memberId);
    }

    /**
     * Authorization 헤더 생성 (Bearer prefix 포함)
     */
    public static String createAuthHeader(String token) {
        return "Bearer " + token;
    }
}
