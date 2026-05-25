package com.lianpayhub.common.error;

public enum ErrorCode {
    BAD_REQUEST(400, "请求参数不正确"),
    FORBIDDEN(403, "没有权限执行该操作"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源状态冲突"),
    RATE_LIMITED(429, "请求过于频繁"),
    SERVER_ERROR(500, "服务器异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
