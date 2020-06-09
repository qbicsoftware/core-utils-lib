package life.qbic.dss.registration.usecases.dtos

/**
 * <add class description here>
 *
 * @author: Sven Fillinger
 */
final class UploadSample {

    private final String externalId

    private final String qbicSampleId

    UploadSample(String externalId, String qbicSampleId) {
        this.externalId = externalId
        this.qbicSampleId = qbicSampleId
    }

}
