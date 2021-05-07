package life.qbic.utils

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

    DataParserException() {
        super()
    }

    DataParserException(String message) {
        super(message)
    }

    DataParserException(String message, Throwable cause) {
        super(message, cause)
    }

}
