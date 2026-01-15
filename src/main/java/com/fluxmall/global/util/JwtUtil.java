package com.fluxmall.global.util;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.auth.exception.AuthError;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessValidity;
    private final long refreshValidity;

    public JwtUtil(
        @Value("${jwt.access-secret}") String accessKey,
        @Value("${jwt.refresh-secret}") String refreshKey,
        @Value("${jwt.access-expiration}") long accessValidity,
        @Value("${jwt.refresh-expiration}") long refreshValidity
    ) {
        // HMAC SHA-512 알고리즘을 사용하는 키 생성
        this.accessKey = Keys.hmacShaKeyFor(accessKey.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(refreshKey.getBytes());
        this.accessValidity = accessValidity;
        this.refreshValidity = refreshValidity;
    }

    public long getExpiration(String accessToken) {
        Date expiration = Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(accessToken)
            .getPayload()
            .getExpiration();

        long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    /**
     * Access Token 생성 (subject = memberId, role claim 포함)
     * @subject: memberId
     * @claim: role
     */
    public String createAccessToken(Long memberId, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessValidity);

        return Jwts.builder()
            .subject(String.valueOf(memberId))  // subject에 memberId 저장
            .claim("role", role)
            .issuedAt(now)
            .expiration(validity)
            .signWith(accessKey, Jwts.SIG.HS512)
            .compact();
    }

    /**
     * Refresh Token 생성 (subject = memberId)
     */
    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshValidity);

        return Jwts.builder()
            .subject(String.valueOf(memberId))  // subject에 memberId 저장
            .issuedAt(now)
            .expiration(validity)
            .signWith(refreshKey, SIG.HS512)
            .compact();
    }

    /**
     * Refresh Token에서 memberId 추출 (subject에서)
     */
    public Long getRefreshMemberId(String token) {
        String subject = Jwts.parser()
            .verifyWith(refreshKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
        return Long.parseLong(subject);
    }

    /**
     * Access Token에서 memberId 추출 (subject에서)
     */
    public Long getMemberId(String token) {
        String subject = Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
        return Long.parseLong(subject);
    }

    public String getRole(String token) {
        return Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("role", String.class);
    }

    /**
     * Access Token 유효성 검증 (상세한 에러 타입 구분)
     * @param token 검증할 토큰
     * @return 유효하면 true
     * @throws BusinessException 토큰이 유효하지 않은 경우 (만료, 잘못된 형식, 지원하지 않는 형식 등)
     */
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.warn("잘못된 JWT 서명입니다. token: {}", token);
            throw new BusinessException(AuthError.INVALID_TOKEN);
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 형식입니다. token: {}", token);
            throw new BusinessException(AuthError.MALFORMED_TOKEN);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다. token: {}", token);
            throw new BusinessException(AuthError.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다. token: {}", token);
            throw new BusinessException(AuthError.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있습니다. token: {}", token);
            throw new BusinessException(AuthError.INVALID_TOKEN);
        }
    }

    /**
     * Refresh Token 유효성 검증 (상세한 에러 타입 구분)
     * @param token 검증할 토큰
     * @return 유효하면 true
     * @throws BusinessException 토큰이 유효하지 않은 경우
     */
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.warn("잘못된 JWT 서명입니다. (Refresh Token)");
            throw new BusinessException(AuthError.INVALID_TOKEN);
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 형식입니다. (Refresh Token)");
            throw new BusinessException(AuthError.MALFORMED_TOKEN);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다. (Refresh Token)");
            throw new BusinessException(AuthError.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다. (Refresh Token)");
            throw new BusinessException(AuthError.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있습니다. (Refresh Token)");
            throw new BusinessException(AuthError.INVALID_TOKEN);
        }
    }
}
