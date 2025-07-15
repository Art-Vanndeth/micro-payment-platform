package com.pipay.account.exception;

import com.pipay.account.common.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> handleResponseStatusException(ResponseStatusException e) {
        ErrorResponse<?> errorResponse = ErrorResponse.builder()
                .code(e.getStatusCode().value())
                .reason(e.getReason())
                .build();
        return ResponseEntity
                .status(e.getStatusCode())
                .body(Map.of("error", errorResponse));
    }

     @ExceptionHandler(AccountNotFoundException.class)
     public ResponseEntity<?> handleAccountNotFound(AccountNotFoundException ex) {
            ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .reason(ex.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
     }
}
