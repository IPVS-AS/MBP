package org.citopt.connde.exception;

public class HashMismatch extends RuntimeException {
    public HashMismatch() {
    }

    public HashMismatch(String message) {
        super(message);
    }

    public HashMismatch(String message, Throwable cause) {
        super(message, cause);
    }

    public HashMismatch(Throwable cause) {
        super(cause);
    }

    public HashMismatch(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
