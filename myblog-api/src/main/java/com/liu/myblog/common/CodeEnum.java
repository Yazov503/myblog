package com.liu.myblog.common;

import lombok.Getter;

@Getter
public enum CodeEnum {

    REQUEST_SUCCEED(200, ""),

    REQUEST_FAILED(401, "请求失败"),

    PARAM_ERROR(402, "参数错误"),

    URI_NOT_EXIST(404, "访问路径不存在"),

    TOO_MANY_REQUESTS(429, "短时间内请求重复太多"),

    SYSTEM_ERROR(501, "系统错误"),

    JW_SYSTEM_ERROR(502, "教务系统错误"),

    JSON_DESERIALIZE_ERROR(5003, "Json解析错误"),

    PICTURE_UPLOAD_ERROR(601, "图片上传错误"),

    PICTURE_DELETE_ERROR(602, "图片删除错误"),

    INVALID_PICTURE_TYPE(603, "非法的图片类型"),

    MUTE_ERROR(701, "禁言出错"),

    AUTH_ERROR(403, "认证失败"),
    PARSE_ERROR(900, "解析失败");

    private final String msg;
    private final Integer code;

    CodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


}
