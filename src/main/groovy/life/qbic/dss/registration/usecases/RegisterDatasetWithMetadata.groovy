package life.qbic.dss.registration.usecases

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import life.qbic.dss.registration.usecases.entities.UploadMetaData
import life.qbic.dss.registration.usecases.entities.UploadSample

/**
 * Orchestration of dataset registration with metadata
 *
 *
 *
 * @author: Sven Fillinger
 */
class RegisterDatasetWithMetadata implements DataSetRegistrationInput{

    private final DataSetRegistrationDataSource dataSource

    private final DataSetRegistrationOutput output

    private UploadMetaData uploadMetaData

    RegisterDatasetWithMetadata(DataSetRegistrationDataSource dataSource,
                                DataSetRegistrationOutput output) {
        this.dataSource = dataSource
        this.output = output
    }

    @Override
    void registerDataSet(UploadMetaData metaData, UploadSample sample) {

    }
}
