package com.fluxmall.member.dto.request;

import com.fluxmall.global.annotation.Password;
import com.fluxmall.member.domain.Member;
import com.fluxmall.member.domain.Member.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    String username,

    @Password
    String password,

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 15, message = "닉네임은 2-15자 사이여야 합니다")
    String nickname
) {
    public Member toEntity(String encodedPassword){
        return Member.builder()
            .username(username)
            .password(encodedPassword)
            .nickname(nickname)
            .role(MemberRole.USER)
            .build();
    }
}
