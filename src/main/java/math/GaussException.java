package math;

/**
 * {@link Exception} type thrown by Gaussian elimination.
 * HUN: Gauss-elimináció álltal dobott kivételtípus.
 * 
 * @author Zoltán Simon
 *
 */
public class GaussException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8704961831467105853L;

	/**
	 * Constructs a new GaussException with no detail message.
	 */
	public GaussException() {
		super();
	}

	/**
	 * Constructs a new GaussException with the specified detail message.
	 *
	 * @param message the detail message
	 */
	public GaussException(String message) {
		super(message);
	}

	/**
	 * Constructs a new GaussException with the specified detail message and cause.
	 *
	 * @param message the detail message
	 * @param cause   the cause
	 */
	public GaussException(String message, Throwable cause) {
		super(message, cause);
	}
}
