package com.pipay.payment.exception;

import com.pipay.payment.common.BaseResult;
import com.pipay.payment.constant.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import static com.pipay.payment.constant.error.ErrorCode.VALIDATION_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public Mono<ResponseEntity<BaseResult<Object>>> handleCustomException(ApiException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus httpStatus = mapToHttpStatus(errorCode);
        BaseResult<Object> error = BaseResult.failure(ex.getErrorCode());
        return Mono.just(ResponseEntity
                .status(httpStatus)
                .body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<BaseResult<Object>>> handleGenericException(Exception ex) {
        log.error(ex.getMessage(), ex);
        BaseResult<Object> response = BaseResult.failure(VALIDATION_ERROR);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Object> handleValidationErrors(WebExchangeBindException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getFieldErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .toList());
    }

    private HttpStatus mapToHttpStatus(ErrorCode code) {
        return switch (code) {
            case ACCOUNT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}
