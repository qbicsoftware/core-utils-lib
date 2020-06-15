package life.qbic.dss.registration.usecases

import life.qbic.dss.registration.usecases.dtos.UploadMetaData
import life.qbic.dss.registration.usecases.dtos.UploadSample

interface DataSetRegistrationInput {

    void registerDataSet(UploadMetaData metaData, UploadSample sample)

}