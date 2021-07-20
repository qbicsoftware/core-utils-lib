package life.qbic.datasets.parsers

import org.everit.json.schema.ValidationException
import spock.lang.Specification

/**
 * Tests for the {@link DatasetValidationException} class.
 *
 * @since 1.9.3
 */
class DatasetValidationExceptionSpec extends Specification {

    def "Passing null as a constructor argument must throw a NPE"() {
        given:
        def validationException = null

        when:
        new DatasetValidationException(validationException)

        then:
        thrown(NullPointerException)
    }

    def "Passing a valid ValidationException as a constructor argument must work"() {
        given:
        ValidationException validationException = new ValidationException("Test message")

        when:
        DatasetValidationException wrapperException = new DatasetValidationException(validationException)

        then:
        wrapperException.getAllExceptions()[0] == "#: Test message"
    }

}
