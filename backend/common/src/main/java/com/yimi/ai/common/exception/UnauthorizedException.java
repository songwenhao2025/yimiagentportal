package com.yimi.ai.common.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("未授权访问");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}