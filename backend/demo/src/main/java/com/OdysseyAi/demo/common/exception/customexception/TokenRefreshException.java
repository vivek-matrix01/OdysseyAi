package com.OdysseyAi.demo.common.exception.customexception;

public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String message) {
        super(message);
    }
}