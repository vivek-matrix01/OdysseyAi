package com.OdysseyAi.demo.common.exception.customexception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}