package com.liu.myblog.exception;

import com.liu.myblog.common.CodeEnum;
import lombok.Getter;

/**
 * @author zsw
 * @date 2019/6/21 16:05
 */
@Getter
public class BaseException extends RuntimeException {
    private Integer code;
    private String msg;


    public BaseException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public BaseException(CodeEnum errCode) {
        this.code = errCode.getCode();
        this.msg = errCode.getMsg();
    }

    public BaseException(CodeEnum errCode, String msg) {
        this.code = errCode.getCode();
        this.msg = msg;
    }
}
