package com.fluxmall.member.dto.response;

import com.fluxmall.member.domain.Member;

public record MemberResponse(
    Long id,
    String username,
    String nickname,
    String role
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getUsername(),
            member.getNickname(),
            member.getRole().name()
        );
    }
}
