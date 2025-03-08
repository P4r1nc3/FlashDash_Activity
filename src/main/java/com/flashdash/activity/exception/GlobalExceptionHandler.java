package com.flashdash.activity.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorDetailsProvider errorDetailsProvider;

    public GlobalExceptionHandler(ErrorDetailsProvider errorDetailsProvider) {
        this.errorDetailsProvider = errorDetailsProvider;
    }

    @ExceptionHandler(FlashDashException.class)
    public ResponseEntity<ErrorResponse> handleFlashDashException(FlashDashException ex) {
        ErrorResponse errorResponse = errorDetailsProvider.createErrorResponse(ex.getErrorCode());
        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }
}
