package com.client_app.exceptions;

public class AuthRuntimeException extends RuntimeException {
    public AuthRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
