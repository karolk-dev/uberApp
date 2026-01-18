package com.driver_app.exceptions;

public class InvalidDriverStatusException extends RuntimeException {
    public InvalidDriverStatusException(String message) {
        super(message);
    }
}
