package de.ipvs.as.mbp.error;

import de.ipvs.as.mbp.util.S;


public class InvalidPasswordException extends Exception {

    public InvalidPasswordException() {
        super();
    }

    public InvalidPasswordException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return S.notEmpty(super.getMessage()) ? super.getMessage() : "Invalid password!";
    }

}
