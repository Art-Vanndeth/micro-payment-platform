package com.pipay.payment.exception;


import com.pipay.payment.constant.error.ErrorCode;

public class CustomException extends ApiException {

    public CustomException(ErrorCode errorCode) {
        super(errorCode);
    }
}
