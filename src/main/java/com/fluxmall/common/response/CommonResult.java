package com.fluxmall.common.response;

import com.fluxmall.exception.BusinessException;
import com.fluxmall.exception.errors.ErrorCode;
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
