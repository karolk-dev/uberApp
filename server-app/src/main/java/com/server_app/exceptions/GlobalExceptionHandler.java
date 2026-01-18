package com.server_app.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvoiceProcessingException.class)
    public ResponseEntity<ErrorMessage> handleInvoiceProcessingException(InvoiceProcessingException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleRouteNotFoundException(RouteNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleRideNotFoundException(RideNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RouteInfoException.class)
    public ResponseEntity<ErrorMessage> handleRouteInfoException(RouteInfoException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
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
