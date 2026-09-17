package com.riverside.permits.controller;

import com.riverside.permits.service.ApiException;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> api(ApiException exception) {
        return response(exception.getStatusCode(), exception.code, exception.getReason(), exception.field);
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> status(ResponseStatusException exception) {
        String code = exception.getStatusCode().value() == 404 ? "PERMIT_NOT_FOUND" : "REQUEST_INVALID";
        String message = exception.getReason() == null ? "Request failed." : exception.getReason();
        return response(exception.getStatusCode(), code, message, null);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            DateTimeParseException.class
    })
    ResponseEntity<ApiError> invalid(Exception exception) {
        String field = exception instanceof MethodArgumentNotValidException validation
                && validation.getBindingResult().getFieldError() != null
                        ? validation.getBindingResult().getFieldError().getField()
                        : null;
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request parameters are invalid.", field);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable() {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request body is invalid.", null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> internal(Exception exception) {
        log.error("Unhandled API error", exception);
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "REQUEST_FAILED",
                "The request could not be completed.",
                null);
    }

    private ResponseEntity<ApiError> response(HttpStatusCode status, String code, String message, String field) {
        return ResponseEntity.status(status).body(new ApiError(
                code,
                message == null ? "Request failed." : message,
                field == null ? "" : field));
    }

    record ApiError(String code, String message, String field) { }
}
