package life.qbic.dss.registration.usecases.exceptions

class SampleCreationException extends RuntimeException {

    SampleCreationException() {
        super()
    }

    SampleCreationException(String msg) {
        super(msg)
    }

    SampleCreationException(String msg, Throwable t) {
        super(msg, t)
    }

}
