package com.fluxmall.auth.dto.request;

import com.fluxmall.global.annotation.Password;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "이메일은 필수입니다")
    String username,

    @Password
    String password
) { }
