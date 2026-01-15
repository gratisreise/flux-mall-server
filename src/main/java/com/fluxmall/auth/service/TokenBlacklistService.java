package com.fluxmall.auth.service;

import com.fluxmall.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final static String PREFIX = "BL:";

    /**
     * Access Token을 블랙리스트에 추가
     */
    public void addAccessToken(String memberId, String accessToken){
        String key = generateKey(memberId);
        long expiration = jwtUtil.getExpiration(accessToken);
        redisTemplate.opsForValue().set(
            key,
            accessToken,
            expiration,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * 토큰을 블랙리스트에 추가 (memberId 추출하여 사용)
     */
    public void addToBlacklist(String token) {
        Long memberId = jwtUtil.getMemberId(token);
        addAccessToken(String.valueOf(memberId), token);
    }

    /**
     * 로그아웃 여부 확인 (Access Token이 블랙리스트에 있는지 확인)
     */
    public boolean isLogout(String memberId, String accessToken){
        String key = generateKey(memberId);
        String storedToken = redisTemplate.opsForValue().get(key);
        return storedToken != null && storedToken.equals(accessToken);
    }

    private static String generateKey(String memberId) {
        return PREFIX + memberId;
    }
}
