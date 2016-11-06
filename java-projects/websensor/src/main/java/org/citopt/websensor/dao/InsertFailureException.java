package org.citopt.websensor.dao;

public class InsertFailureException extends Exception {

    public InsertFailureException() {
    }

    public InsertFailureException(String message) {
        super(message);
    }

    public InsertFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsertFailureException(Throwable cause) {
        super(cause);
    }
        
}
