package com.driver_app.exceptions;

public class DuplicateDriverException extends RuntimeException {
    public DuplicateDriverException(String message) {
        super(message);
    }
}
