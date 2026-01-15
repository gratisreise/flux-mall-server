package com.fluxmall.global.response;

import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult {
    private String code;
    private String message;

    public static CommonResult filtered(BusinessException e){
        ErrorCode errorCode = e.getErrorCode();
        return new CommonResult(errorCode.getCode(), errorCode.getMessage());
    }
}
