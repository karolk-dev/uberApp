package com.server_app.exceptions;

public class InvoiceProcessingException extends RuntimeException {
    public InvoiceProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
