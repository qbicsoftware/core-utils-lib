package life.qbic.dss.registration.usecases

import life.qbic.dss.registration.usecases.entities.UploadMetaData

interface DataSetRegistrationOutput {

    void confirmRegistration(UploadMetaData uploadMetaData, String usedTestSample)

    void invokeOnError(String msg)
}