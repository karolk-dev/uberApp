package com.client_app.exceptions;

public class DuplicateClientException extends RuntimeException {
    public DuplicateClientException(String message) {
        super(message);
    }
}
