package com.fluxmall.domain.dto;


import com.fluxmall.annotations.Password;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank
    String username,
    @Password
    String password,
    @NotBlank
    String nickname
) { }
