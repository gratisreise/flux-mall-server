package com.fluxmall.service.auth;

import com.fluxmall.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final static String PREFIX = "BL";

    public void addAccessToken(String memberId, String accessToken){
        String key = generateKey(memberId);
        long expiration = jwtUtil.getExpiration(accessToken);
        redisTemplate.opsForValue().set(
            key,
            accessToken,
            expiration
        );
    }

    public boolean isLogout(String memberId, String accessToken){
        String key = generateKey(memberId);
        String storedToken = redisTemplate.opsForValue().get(key);
        return storedToken != null && storedToken.equals(accessToken);
    }

    private static String generateKey(String memberId) {
        return PREFIX + memberId;
    }
}
