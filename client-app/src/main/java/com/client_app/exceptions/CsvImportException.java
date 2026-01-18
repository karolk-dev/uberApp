package com.client_app.exceptions;

public class CsvImportException extends RuntimeException {
    public CsvImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
