package com.liu.myblog.common;

import lombok.Getter;

@Getter
public enum VerificationCode {

    REGISTER(1, "注册验证码"),
    RESET_EMAIL(2, "邮箱换绑"),
    LOGIN(3,"登录验证码"),
    RESET_PASSWORD(4, "修改密码");

    private int code;
    private String message;

    VerificationCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
