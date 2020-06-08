package life.qbic.dss.registration.usecases.exceptions

/**
 * Class that represents data source access exceptions.
 *
 * @author: Sven Fillinger
 */
class ResourceAccessException extends RuntimeException {

    ResourceAccessException() {
        super()
    }

    ResourceAccessException(String msg) {
        super(msg)
    }

    ResourceAccessException(String msg, Throwable t) {
        super(msg, t)
    }

}
