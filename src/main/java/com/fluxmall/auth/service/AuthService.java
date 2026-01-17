package com.fluxmall.auth.service;

import com.fluxmall.auth.dto.request.LoginRequest;
import com.fluxmall.auth.dto.response.LoginResponse;
import com.fluxmall.auth.dto.response.LogoutResponse;
import com.fluxmall.auth.dto.response.TokenRefreshResponse;
import com.fluxmall.auth.exception.AuthError;
import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.global.util.JwtUtil;
import com.fluxmall.member.domain.Member;
import com.fluxmall.member.repository.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;



    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        Member member = memberMapper.findByUsername(request.username());
        if (member == null) {
            throw new BusinessException(AuthError.INVALID_CREDENTIALS);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(AuthError.INVALID_CREDENTIALS);
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(member.getId());

        return LoginResponse.of(accessToken, refreshToken);
    }

    /**
     * 로그아웃
     */
    public LogoutResponse logout(String token) {
        tokenBlacklistService.addToBlacklist(token);
        return LogoutResponse.success();
    }

    /**
     * Access Token 재발급
     */
    @Transactional(readOnly = true)
    public TokenRefreshResponse refresh(String refreshToken) {
        // Refresh Token 검증 (validateRefreshToken 내부에서 예외 발생)
        jwtUtil.validateRefreshToken(refreshToken);

        // 새로운 Access Token 발급
        Long memberId = jwtUtil.getRefreshMemberId(refreshToken);
        Member member = memberMapper.findById(memberId);
        if (member == null) {
            throw new BusinessException(AuthError.MEMBER_NOT_FOUND);
        }

        String newAccessToken = jwtUtil.createAccessToken(member.getId(), member.getRole().name());
        return TokenRefreshResponse.of(newAccessToken);
    }
}
