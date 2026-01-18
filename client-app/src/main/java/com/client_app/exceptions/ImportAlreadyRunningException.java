package com.client_app.exceptions;

public class ImportAlreadyRunningException extends RuntimeException {
    public ImportAlreadyRunningException(String message) {
        super(message);
    }
}
