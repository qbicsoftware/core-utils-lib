package life.qbic.dss.registration.usecases.dtos


/**
 * <add class description here>
 *
 * @author: Sven Fillinger
 */
final class UploadMetaData {

    private final List<URL> incomingFiles

    private final ExperimentType type

    private final UploadSample sample

    UploadMetaData(List<URL> incomingFiles, ExperimentType type, UploadSample sample) {
        this.incomingFiles = new ArrayList<>()
        copyIncomingFiles(incomingFiles)
        this.type = type
        this.sample = sample
    }

    private void copyIncomingFiles(List<URL> incomingFiles) {
        Objects.requireNonNull(incomingFiles, "Incoming files must not be null!")
        incomingFiles.each { this.incomingFiles.add(it) }
    }

}
