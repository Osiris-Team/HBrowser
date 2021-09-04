package com.osiris.headlessbrowser.javascript.exceptions;

public class DuplicateRegisteredId extends Exception{

    public DuplicateRegisteredId() {
    }

    public DuplicateRegisteredId(String message) {
        super(message);
    }

    public DuplicateRegisteredId(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateRegisteredId(Throwable cause) {
        super(cause);
    }

    public DuplicateRegisteredId(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
