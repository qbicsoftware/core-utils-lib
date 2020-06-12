package life.qbic.dss.registration.datasources

import life.qbic.datamodel.samples.OpenbisTestSample
import life.qbic.dss.registration.usecases.DataSetRegistrationDataSource

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2


/**
 * A data source that can be provided during ETL processes.
 *
 * The class basically wraps a
 *      ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
 * class instance that is provided during the DSS runtime of openBIS.
 *
 * It provides different methods for openBIS data source interactions that need to be executed from central use cases
 * during data set registration at QBiC.
 *
 * @author: Sven Fillinger
 */
class ETLDataSource implements DataSetRegistrationDataSource {

    private final IDataSetRegistrationTransactionV2 transaction

    ETLDataSource(IDataSetRegistrationTransactionV2 transaction) {
        this.transaction = transaction
    }

    @Override
    String createNewTestSample(String parentBioSampleCode, String type) {
        return "test"
    }

    @Override
    String createNewAnalysisRunSample(String parentTestSampleCode) {
        return null
    }

    @Override
    String determineSampleType(String sampleCode) {
        return null
    }

    @Override
    OpenbisTestSample findTestSample(String sampleCode) {
        return null
    }

    @Override
    String registerDataSet(List<URL> dataSetFiles, String runSampleCode) {
        return null
    }
}
