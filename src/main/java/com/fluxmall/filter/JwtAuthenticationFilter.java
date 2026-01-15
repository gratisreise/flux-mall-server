package com.fluxmall.filter;

import com.fluxmall.common.CommonValue;
import com.fluxmall.exception.BusinessException;
import com.fluxmall.exception.errors.AuthError;
import com.fluxmall.service.auth.TokenBlacklistService;
import com.fluxmall.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 인증 필터
 * 요청의 Authorization 헤더에서 JWT 토큰을 추출하고 검증하여 SecurityContext에 인증 정보를 설정합니다.
 *
 * 동작 순서:
 * 1. Authorization 헤더에서 Bearer 토큰 추출
 * 2. JWT 토큰 유효성 검증 (서명, 만료, 형식 등)
 * 3. 블랙리스트 검증 (로그아웃된 토큰인지 확인)
 * 4. SecurityContext에 인증 정보 설정
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        try {
            String accessToken = extractAccessToken(request);

            // 토큰이 존재하는 경우에만 검증 진행
            if (StringUtils.hasText(accessToken)) {
                authenticateToken(accessToken);
            }

            // 다음 필터로 진행
            filterChain.doFilter(request, response);

        } catch (BusinessException e) {
            // 인증 실패 시 예외를 다시 던져 ExceptionHandlerFilter에서 처리하도록 함
            log.error("JWT 인증 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정합니다.
     *
     * @param accessToken 검증할 JWT 토큰
     * @throws BusinessException 토큰이 유효하지 않거나 블랙리스트에 등록된 경우
     */
    private void authenticateToken(String accessToken) {
        // 1. 토큰 유효성 검증 (JwtUtil에서 상세한 예외 처리)
        jwtUtil.validateAccessToken(accessToken);

        // 2. 토큰에서 사용자 정보 추출
        Long memberId = jwtUtil.getMemberId(accessToken);
        String role = jwtUtil.getRole(accessToken);

        // 3. 블랙리스트 검증 (로그아웃된 토큰인지 확인)
        if (tokenBlacklistService.isLogout(String.valueOf(memberId), accessToken)) {
            log.warn("블랙리스트에 등록된 토큰입니다. memberId: {}", memberId);
            throw new BusinessException(AuthError.BLACKLISTED_TOKEN);
        }

        // 4. SecurityContext에 인증 정보 설정
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                memberId,  // principal = memberId (Long 타입)
                null,      // credentials = null (비밀번호 불필요)
                AuthorityUtils.createAuthorityList("ROLE_" + role)  // authorities
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("JWT 인증 성공. memberId: {}, role: {}", memberId, role);
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 Bearer 토큰을 추출합니다.
     *
     * @param request HTTP 요청
     * @return Bearer 토큰 문자열 (토큰이 없거나 형식이 올바르지 않으면 null)
     */
    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(CommonValue.AUTH_PREFIX)) {
            return bearerToken.substring(CommonValue.AUTH_PREFIX.length());
        }
        return null;
    }
}
