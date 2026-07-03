package com.OdysseyAi.demo.common.exception.customexception;

//ForbiddenException.java (e.g., Valid token, but lacks Admin role)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}