package com.OdysseyAi.demo.common.exception.customexception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) { super(message); }
}