package com.OdysseyAi.demo.common.exception.customexception;

public class UnsupportedLocationException extends RuntimeException {

    public UnsupportedLocationException() {
        super("Sorry, we do not provide services at this location.");
    }

    public UnsupportedLocationException(String message) {
        super(message);
    }
}
