package com.fluxmall.member.controller;

import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import com.fluxmall.member.dto.request.RegisterRequest;
import com.fluxmall.member.dto.response.MemberResponse;
import com.fluxmall.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 API")
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입
     */
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    public CommonResult register(@Valid @RequestBody RegisterRequest request) {
        memberService.register(request);
        return ResponseService.getSuccessResult();
    }

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 회원의 정보를 조회합니다.")
    public SingleResult<MemberResponse> getMyInfo(@CurrentMemberId Long memberId) {
        return ResponseService.getSingleResult(memberService.getMyInfo(memberId));
    }

    /**
     * 내 정보 수정 (닉네임)
     */
    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 회원의 닉네임을 수정합니다.")
    public SingleResult<MemberResponse> updateMyInfo(
            @CurrentMemberId Long memberId,
            @RequestParam String nickname
    ) {
        return ResponseService.getSingleResult(memberService.updateProfile(memberId, nickname));
    }
}
