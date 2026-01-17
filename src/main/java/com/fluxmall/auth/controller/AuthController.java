package com.fluxmall.auth.controller;

import com.fluxmall.auth.dto.request.LoginRequest;
import com.fluxmall.auth.dto.response.LoginResponse;
import com.fluxmall.auth.dto.response.LogoutResponse;
import com.fluxmall.auth.dto.response.TokenRefreshResponse;
import com.fluxmall.auth.service.AuthService;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
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

    private final AuthService authService;

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다.")
    public SingleResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseService.getSingleResult(authService.login(request));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Refresh Token을 블랙리스트에 추가합니다.")
    public SingleResult<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return ResponseService.getSingleResult(authService.logout(token));
    }

    /**
     * Access Token 재발급
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    public SingleResult<TokenRefreshResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return ResponseService.getSingleResult(authService.refresh(token));
    }
}
