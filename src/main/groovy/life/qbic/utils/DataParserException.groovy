package life.qbic.utils

/**
 * Exception that shall be thrown if parsing of a data structures fail.
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
