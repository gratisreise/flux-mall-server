package com.fluxmall.domain.dto;


public record LoginResponse (
    String accessToken,
    String refreshToken
){ }
