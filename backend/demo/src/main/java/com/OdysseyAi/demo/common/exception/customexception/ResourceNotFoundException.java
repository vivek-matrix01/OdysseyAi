package com.OdysseyAi.demo.common.exception.customexception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}