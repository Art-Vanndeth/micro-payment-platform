package com.pipay.payment.common;

import com.pipay.payment.constant.error.ErrorCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class BaseResult<T> {

    private Integer responseCode;
    private String errorCode;
    private String responseMessage;
    private T data;

    public static <T> BaseResult<T> success(T data, String message) {
        return new BaseResult<>(0, null, message, data);
    }

    public static <T> BaseResult<T> failure(ErrorCode errorCode) {
        return new BaseResult<>(1, errorCode.getCode(), errorCode.getMessage(), null);
    }

}
