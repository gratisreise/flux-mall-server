package com.fluxmall.controller;


import com.fluxmall.common.response.ResponseService;
import com.fluxmall.common.response.SingleResult;
import com.fluxmall.domain.dto.LoginRequest;
import com.fluxmall.domain.dto.LoginResponse;
import com.fluxmall.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "회원가입")
    public SingleResult<LoginResponse> register(@Valid LoginRequest request){
        return ResponseService.getSingleResult(memberService.login(request));
    }
}