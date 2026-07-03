package com.OdysseyAi.demo.common.exception.customexception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}