package com.fluxmall.filter;

import com.fluxmall.common.CommonValue;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String accessToken = extractAccessToken(request);

        //토큰 존재 검증 및 존재 검증
        if (StringUtils.hasText(accessToken) && jwtUtil.validateAccessToken(accessToken)) {
            String username = jwtUtil.getUsername(accessToken);

            //토큰 블랙리스트 검증
            if(!tokenBlacklistService.isLogout(username, accessToken)){
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                //검증
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("블랙 리스트에 등록된 토큰입니다.");
            }
        }

        //넘기기
        filterChain.doFilter(request, response);
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(CommonValue.AUTH_PREFIX)) {
            return bearerToken.substring(CommonValue.AUTH_PREFIX.length());
        }
        return null;
    }
}
