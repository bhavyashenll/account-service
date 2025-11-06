package com.digi.account.exception;

public class PdfGenerationException extends StatementException {
    public PdfGenerationException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}