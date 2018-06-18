package life.qbic.exceptions;

/**
 * Handy runtime exception to avoid.
 */
public class ApplicationException extends RuntimeException {

  public ApplicationException(final String message) {
    super(message);
  }

  public ApplicationException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
