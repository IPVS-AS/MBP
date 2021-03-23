package de.ipvs.as.mbp.error;

import de.ipvs.as.mbp.util.S;

/**
 * Thrown in case somebody tries to login into a user for which login is not possible, e.g. because it is a system user.
 */
public class UserNotLoginableException extends RuntimeException {

    public UserNotLoginableException() {
        super();
    }

    public UserNotLoginableException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return S.notEmpty(super.getMessage()) ? super.getMessage() : "It is not possible to login into this user.";
    }

}
