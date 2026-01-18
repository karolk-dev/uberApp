package com.client_app.exceptions;

public class LoginFailedException extends RuntimeException {
    public LoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
