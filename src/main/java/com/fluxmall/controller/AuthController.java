package com.fluxmall.controller;

import com.fluxmall.common.response.ResponseService;
import com.fluxmall.common.response.SingleResult;
import com.fluxmall.domain.dto.LoginRequest;
import com.fluxmall.domain.dto.LoginResponse;
import com.fluxmall.service.MemberService;
import com.fluxmall.service.auth.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final MemberService memberService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ResponseService responseService;

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다.")
    public SingleResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = memberService.authenticate(request);
        return responseService.getSingleResult(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Refresh Token을 블랙리스트에 추가합니다.")
    public SingleResult<String> logout(@RequestHeader("Authorization") String refreshToken) {
        // Bearer 접두사 제거
        String token = refreshToken.replace("Bearer ", "");
        tokenBlacklistService.addToBlacklist(token);
        return responseService.getSingleResult("로그아웃되었습니다.");
    }

    /**
     * Access Token 재발급
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    public SingleResult<String> refresh(@RequestHeader("Authorization") String refreshToken) {
        // 실제 구현은 JwtUtil의 검증 로직과 연동 필요
        String token = refreshToken.replace("Bearer ", "");
        // TODO: Refresh Token 검증 후 새로운 Access Token 발급
        return responseService.getSingleResult("새로운 Access Token이 발급되었습니다.");
    }
}
