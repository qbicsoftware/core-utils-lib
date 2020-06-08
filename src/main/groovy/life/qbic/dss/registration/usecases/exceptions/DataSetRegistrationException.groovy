package life.qbic.dss.registration.usecases.exceptions

/**
 * Class that represents exceptions that occur during
 * data set registration.
 *
 * @author: Sven Fillinger
 */
class DataSetRegistrationException extends RuntimeException {

    DataSetRegistrationException() {
        super()
    }

    DataSetRegistrationException(String msg) {
        super(msg)
    }

    DataSetRegistrationException(String msg, Throwable t) {
        super(msg, t)
    }

}
