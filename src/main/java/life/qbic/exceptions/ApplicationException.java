package life.qbic.exceptions;

/**
 * Handy all-encompassing runtime exception.
 */
public class ApplicationException extends RuntimeException {

    /**
     * @param message a message.
     */
    public ApplicationException(final String message) {
        super(message);
    }

    /**
     * @param message a message.
     * @param cause the origin of the problem.
     */
    public ApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
