package com.riverside.permits.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ApiException extends ResponseStatusException {
    public final String code;
    public final String field;

    public ApiException(HttpStatus status, String code, String message, String field) {
        super(status, message);
        this.code = code;
        this.field = field;
    }
}
