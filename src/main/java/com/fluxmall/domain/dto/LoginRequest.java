package com.fluxmall.domain.dto;

import com.fluxmall.annotations.Password;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "이메일은 필수입니다")
    String username,

    @Password
    String password
) { }
