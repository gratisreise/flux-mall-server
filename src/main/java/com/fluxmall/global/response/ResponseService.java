package com.fluxmall.global.response;




import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.global.exception.ResultCode;
import java.util.List;

public class ResponseService {

    public static <T> ListResult<T> getListResult(List<T> data) {
        ListResult<T> result = new ListResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }

    public static <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }

    public static CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return result;
    }

    // 잡힌에러
    public static CommonResult getFailResult(BusinessException ex) {
        return CommonResult.filtered(ex);
    }

    //못잡힌 에러
    public static CommonResult getFailResult(RuntimeException e) {
        CommonResult result = new CommonResult();
        setFailResult(result);
        return result;
    }

    private static void setSuccessResult(CommonResult result) {
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
    }

    private static void setFailResult(CommonResult result) {
        result.setCode(ResultCode.FAILED.getCode());
        result.setMessage(ResultCode.FAILED.getMessage());
    }


    private ResponseService(){}
}
