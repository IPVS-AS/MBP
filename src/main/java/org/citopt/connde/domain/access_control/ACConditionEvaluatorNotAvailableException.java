package org.citopt.connde.domain.access_control;

import org.citopt.connde.service.access_control.ACAbstractConditionEvaluator;

/**
 * Thrown to indicate that a no {@link ACAbstractConditionEvaluator} could
 * be found.
 * 
 * @author Jakob Benz
 */
public class ACConditionEvaluatorNotAvailableException extends Exception {

	private static final long serialVersionUID = 3750638078395814978L;

	/**
	 * Constructs a new exception with {@code null} as its detail message. The cause
	 * is not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 */
	public ACConditionEvaluatorNotAvailableException() {
		super();
	}

	/**
     * Constructs a new exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message.
     */
	public ACConditionEvaluatorNotAvailableException(String message) {
		super(message);
	}

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * <p>
     * Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message.
     * @param cause the {@link Throwable cause} (a {@code null} value is
     * 		  permitted, and indicates that the cause is nonexistent or
     * 		  unknown.)
     */
    public ACConditionEvaluatorNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

}
