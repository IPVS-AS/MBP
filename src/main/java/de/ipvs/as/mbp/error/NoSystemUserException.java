package de.ipvs.as.mbp.error;

import de.ipvs.as.mbp.util.S;

/**
 * Thrown in case a human user tries to perform an action that can only be performed by system users.
 */
public class NoSystemUserException extends Exception {

    public NoSystemUserException() {
        super();
    }

    public NoSystemUserException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return S.notEmpty(super.getMessage()) ? super.getMessage() : "This action is restricted to system users.";
    }
}
