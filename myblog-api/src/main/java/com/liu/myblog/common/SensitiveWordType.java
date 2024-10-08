package com.liu.myblog.common;

public enum SensitiveWordType {
    BLACKLIST(0),
    WHITELIST(1);

    private final int value;

    SensitiveWordType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
