package com.digi.account.exception;

import lombok.Getter;

@Getter
public class StatementException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public StatementException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}