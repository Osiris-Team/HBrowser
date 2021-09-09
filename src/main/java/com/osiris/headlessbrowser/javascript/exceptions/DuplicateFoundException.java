package com.osiris.headlessbrowser.javascript.exceptions;

public class DuplicateFoundException extends Exception {

    public DuplicateFoundException() {
    }

    public DuplicateFoundException(String message) {
        super(message);
    }

    public DuplicateFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateFoundException(Throwable cause) {
        super(cause);
    }

    public DuplicateFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
