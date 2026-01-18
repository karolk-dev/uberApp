package com.driver_app.exceptions;

public class CeidgVerificationException extends RuntimeException {
    public CeidgVerificationException(String message) {
        super(message);
    }

    public CeidgVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
