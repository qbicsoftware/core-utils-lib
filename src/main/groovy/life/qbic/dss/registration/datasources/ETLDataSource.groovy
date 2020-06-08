package life.qbic.dss.registration.datasources

import life.qbic.dss.registration.usecases.QuerySampleInformationDataSource
import life.qbic.dss.registration.usecases.RegisterDataSetDataSource

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
class ETLDataSource implements RegisterDataSetDataSource, QuerySampleInformationDataSource {

}
