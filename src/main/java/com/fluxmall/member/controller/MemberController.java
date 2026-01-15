package com.fluxmall.member.controller;

import com.fluxmall.global.response.ResponseService;
import com.fluxmall.global.response.SingleResult;
import com.fluxmall.member.dto.request.RegisterRequest;
import com.fluxmall.member.domain.Member;
import com.fluxmall.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 API")
public class MemberController {

    private final MemberService memberService;
    private final ResponseService responseService;

    /**
     * 회원가입
     */
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    public SingleResult<Long> register(@Valid @RequestBody RegisterRequest request) {
        Member member = memberService.register(request);
        return responseService.getSingleResult(member.getId());
    }

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 회원의 정보를 조회합니다.")
    public SingleResult<Member> getMyInfo(@AuthenticationPrincipal Long memberId) {
        Member member = memberService.findById(memberId);
        return responseService.getSingleResult(member);
    }

    /**
     * 내 정보 수정 (닉네임)
     */
    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 회원의 닉네임을 수정합니다.")
    public SingleResult<String> updateMyInfo(
            @AuthenticationPrincipal Long memberId,
            @RequestParam String nickname
    ) {
        memberService.updateProfile(memberId, nickname);
        return responseService.getSingleResult("프로필이 수정되었습니다.");
    }
}
