package com.liu.myblog.exception;



import com.liu.myblog.common.CodeEnum;
import com.liu.myblog.common.ReturnData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.security.cert.CertPathValidatorException;


/**
 * @author zsw
 * @date 2019/6/21 16:07
 */
@RestControllerAdvice
public class ExceptionResolver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionResolver.class);
    
    /**
     * 捕获全局业务异常
     */
    @ResponseBody
    @ExceptionHandler(value = BaseException.class)
    public ReturnData BaseExceptionHandler(BaseException e) {
        LOGGER.error("异常：[{}]", e.getMsg());
        return ReturnData.fail(e.getCode(), e.getMsg());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ReturnData ExceptionHandler(Exception e) {
        LOGGER.error("异常：[{}]", e.getMessage());
        return ReturnData.fail(CodeEnum.SYSTEM_ERROR.getCode(), "系统异常");
    }

    @ResponseBody
    @ExceptionHandler(value = {CertPathValidatorException.class, BindException.class, ServletRequestBindingException.class, MethodArgumentNotValidException.class})
    public ReturnData ConstraintViolationExceptionHandler(Exception e) {
        LOGGER.error("-------------->参数异常 {}", e.getMessage());
        return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(),"参数异常。");
    }

    @ResponseBody
    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ReturnData MethodNotSupportedExceptionHandler(Exception e) {
        LOGGER.error("异常：[{}]", e.getMessage());
        return ReturnData.fail(CodeEnum.REQUEST_FAILED.getCode(),"请求方式不被允许");
    }

    @ResponseBody
    @ExceptionHandler(value = {NoHandlerFoundException.class})
    public ReturnData NoHandlerFoundExceptionExceptionHandler(Exception e) {
        LOGGER.error("异常：[{}]", e.getMessage());
        return ReturnData.fail(CodeEnum.URI_NOT_EXIST.getCode(),"访问的资源不存在，请检查访问路径");
    }
}
