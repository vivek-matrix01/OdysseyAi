package com.OdysseyAi.demo.common.exception.customexception;

public class AiGenerationException extends RuntimeException {

    public AiGenerationException(String message) {
        super(message);
    }

    public AiGenerationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}