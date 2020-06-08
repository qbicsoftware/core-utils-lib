package life.qbic.dss.registration.usecases

import life.qbic.dss.registration.usecases.entities.UploadMetaData
import life.qbic.dss.registration.usecases.entities.UploadSample

interface DataSetRegistrationInput {

    void registerDataSet(UploadMetaData metaData, UploadSample sample)

}