package life.qbic.dss.registration.usecases

import life.qbic.datamodel.samples.OpenbisTestSample
import life.qbic.dss.registration.usecases.exceptions.DataSetRegistrationException
import life.qbic.dss.registration.usecases.exceptions.ResourceAccessException


interface DataSetRegistrationDataSource {

    String createNewTestSample(String parentBioSampleCode, String type) throws DataSetRegistrationException

    String createNewAnalysisRunSample(String parentTestSampleCode) throws DataSetRegistrationException

    String determineSampleType(String sampleCode) throws ResourceAccessException

    OpenbisTestSample findTestSample(String sampleCode)

    String registerDataSet(List<URL> dataSetFiles, String runSampleCode) throws DataSetRegistrationException

}