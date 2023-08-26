package io.arsha.api.exceptions;

import lombok.Getter;

public abstract class AbstractException extends Exception {
    @Getter
    final Integer status;
    @Getter
    ExceptionCode exceptionCode;
    @Getter
    String message = "Bad Request";

    protected AbstractException(int status, ExceptionCode exceptionCode, String message) {
        this.status = status;
        this.exceptionCode = exceptionCode;
        this.message = message;
    }

    protected AbstractException(int status, ExceptionCode exceptionCode) {
        this.status = status;
        this.exceptionCode = exceptionCode;
    }

    protected AbstractException(int status) {
        this.status = status;
        this.exceptionCode = ExceptionCode.GENERIC;
    }

    protected AbstractException() {
        this.status = 500;
        this.exceptionCode = ExceptionCode.GENERIC;
    }

}
