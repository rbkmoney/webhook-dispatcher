package com.rbkmoney.webhook.dispatcher.exception;

public class CantRetryException extends RuntimeException {


    public CantRetryException() {
    }

    public CantRetryException(String message) {
        super(message);
    }

    public CantRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public CantRetryException(Throwable cause) {
        super(cause);
    }

    public CantRetryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
