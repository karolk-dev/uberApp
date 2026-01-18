package com.client_app.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateClientException.class)
    public ResponseEntity<ErrorMessage> handleDuplicateClientException(DuplicateClientException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthRuntimeException.class)
    public ResponseEntity<ErrorMessage> handleAuthRuntimeException(AuthRuntimeException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CsvImportException.class)
    public ResponseEntity<ErrorMessage> handleCsvImportException(CsvImportException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ClientRegistrationException.class)
    public ResponseEntity<ErrorMessage> handleClientRegistrationException(ClientRegistrationException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGlobalException(Exception e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorMessage> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(BAD_REQUEST.value())
                .message(e.getMessage())
                .uri(request.getRequestURI())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<String> handleLoginFailedException(LoginFailedException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleWebClientResponseException(WebClientResponseException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(e.getMessage());
    }

    @ExceptionHandler(ImportAlreadyRunningException.class)
    public ResponseEntity<?> handleImportAlreadyRunning(ImportAlreadyRunningException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap("error", e.getMessage()));
    }

    private ResponseEntity<ErrorMessage> buildErrorResponse(Exception e, HttpServletRequest request, HttpStatus status) {
        ErrorMessage errorMessage = ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(status.value())
                .status(status.getReasonPhrase())
                .message(e.getMessage())
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build();
        return new ResponseEntity<>(errorMessage, status);
    }
}
