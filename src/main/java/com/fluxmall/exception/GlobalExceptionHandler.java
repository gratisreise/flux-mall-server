package com.fluxmall.exception;

import com.fluxmall.common.response.CommonResult;
import com.fluxmall.common.response.ResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleBusinessException(BusinessException e) {
        return ResponseService.getFailResult(e);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult handleRuntimeException(RuntimeException e) {
        return ResponseService.getFailResult(e);
    }
}
