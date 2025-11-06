package com.digi.account.exception;

public class TemplateNotFoundException extends StatementException {
    public TemplateNotFoundException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}