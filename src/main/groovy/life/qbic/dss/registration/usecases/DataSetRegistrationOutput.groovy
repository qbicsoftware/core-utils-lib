package life.qbic.dss.registration.usecases

import life.qbic.dss.registration.usecases.dtos.UploadMetaData

interface DataSetRegistrationOutput {

    void confirmRegistration(UploadMetaData uploadMetaData, String usedTestSample)

    void invokeOnError(String msg)
}