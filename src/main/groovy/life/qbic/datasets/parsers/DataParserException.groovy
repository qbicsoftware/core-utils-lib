package life.qbic.datasets.parsers

/**
 * Thrown to indicate that a dataset could not be parsed. 
 * <p>The exception provides several types of information:
 * <ul>
 * <li>a string describing the exception. This is used as the Java exception message.</li>
 * <li>the causal relationship, if any for this <code>DataParserException</code></li>
 * </ul>
 *
 * @since 1.7.0
 */
class DataParserException extends RuntimeException {

    /**
     * Constructs a <code>DataParserException</code> with no detail message
     * @since 1.7.0
     */
    DataParserException() {
        super()
    }

    /**
     * Constructs a <code>DataParserException</code> with the specified detail message.
     * @param message the detail message
     * @since 1.7.0
     */
    DataParserException(String message) {
        super(message)
    }

    /**
     * Constructs a new <code>DataParserException</code> with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @since 1.7.0
     */
    DataParserException(String message, Throwable cause) {
        super(message, cause)
    }

}
