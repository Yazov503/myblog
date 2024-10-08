package com.liu.myblog.common;

import lombok.Getter;

@Getter
public enum BlogStatus {

    PENDING(0, "审核中"),

    APPROVED(1, "通过审核"),

    REJECTED(2, "审核未通过");

    private final String msg;
    private final Integer code;

    BlogStatus(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
