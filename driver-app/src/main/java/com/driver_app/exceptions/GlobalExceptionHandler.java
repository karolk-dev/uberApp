package com.driver_app.exceptions;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateDriverException.class)
    public ResponseEntity<ErrorMessage> handleDuplicateDriverException(DuplicateDriverException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DriverNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleDriverNotFoundException(DriverNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidDriverStatusException.class)
    public ResponseEntity<ErrorMessage> handleInvalidDriverStatusException(InvalidDriverStatusException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CarNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleCarNotFoundException(CarNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorMessage> handleFeignException(FeignException e, HttpServletRequest request) {
        return buildErrorResponse(new RuntimeException("Error occurred while communicating with external service", e), request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PenaltyException.class)
    public ResponseEntity<ErrorMessage> handlePenaltyException(PenaltyException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RideFinishException.class)
    public ResponseEntity<ErrorMessage> handleRideFinishException(RideFinishException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserCreationException.class)
    public ResponseEntity<ErrorMessage> handleUserCreationException(UserCreationException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ProposalRemoveException.class)
    public ResponseEntity<ErrorMessage> handleProposalRemoveException(ProposalRemoveException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ProposalRetrieveException.class)
    public ResponseEntity<ErrorMessage> handleProposalRetrieveException(ProposalRetrieveException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ProposalStoreException.class)
    public ResponseEntity<ErrorMessage> handleProposalStoreException(ProposalStoreException e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGlobalException(Exception e, HttpServletRequest request) {
        return buildErrorResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationExceptions(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        // Zbieramy wszystkie błędy walidacji pól do mapy
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Tworzymy komunikat błędu zawierający wszystkie błędy walidacji
        String validationErrorMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        // Budujemy odpowiedź z kodem 400 Bad Request
        ErrorMessage errorMessage = ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed: " + validationErrorMessage)
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
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