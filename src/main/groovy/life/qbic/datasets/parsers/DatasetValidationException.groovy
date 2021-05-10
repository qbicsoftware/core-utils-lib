package life.qbic.datasets.parsers

import org.everit.json.schema.ValidationException

/**
 * Exception that shall be thrown when a dataset validation failed.
 *
 * <p>This exception class wraps the underlying <code>org.everit.json.schema
 * .ValidationException</code> class, to encapsulate this third party dependency.</p>
 *
 * @since 1.7.0
 */
class DatasetValidationException extends RuntimeException {

    private final ValidationException validationException

    /**
     * Creates a dataset validation exception object wrapping a JSON schema
     * {@link ValidationException}.
     * @param validationException
     * @since 1.7.0
     */
    DatasetValidationException(ValidationException validationException) {
        this.validationException = validationException
    }

    /**
     * Returns all messages of found dataset validations.
     * @return a list of exception messages
     * @since 1.7.0
     */
    List<String> getAllExceptions() {
        return this.validationException.allMessages
    }

    /**
     * Returns a collection of all {@link DatasetValidationException} happened during
     * the validation.
     * @return a list of causal exceptions
     * @since 1.7.0
     */
    List<DatasetValidationException> getCauses() {
        List<DatasetValidationException> convertedExceptions = []
        for (ValidationException validationException : validationException.causingExceptions) {
            convertedExceptions.add(new DatasetValidationException(validationException))
        }
        return convertedExceptions
    }
}
