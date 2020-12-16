package de.ipvs.as.mbp.domain.access_control;

/**
 * Thrown to indicate that an attribute value could not be found
 * or retrieved from an entity.
 * 
 * @author Jakob Benz
 */
public class ACAttributeNotAvailableException extends Exception {

	private static final long serialVersionUID = -4282107838366456742L;

	/**
	 * Constructs a new exception with {@code null} as its detail message. The cause
	 * is not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 */
	public ACAttributeNotAvailableException() {
		super();
	}

	/**
     * Constructs a new exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message.
     */
	public ACAttributeNotAvailableException(String message) {
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
    public ACAttributeNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

}
