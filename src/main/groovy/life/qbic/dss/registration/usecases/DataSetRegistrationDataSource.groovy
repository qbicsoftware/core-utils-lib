package life.qbic.dss.registration.usecases

import life.qbic.datamodel.samples.OpenbisTestSample
import life.qbic.dss.registration.usecases.exceptions.DataSetRegistrationException
import life.qbic.dss.registration.usecases.exceptions.ResourceAccessException


interface DataSetRegistrationDataSource {

    /**
     * Creates a new test sample with an auto-generated sample code of a given sample type and attaches it to another sample as child
     * @param parentSampleCode
     * @param sampleType: The sample type (for example "DNA", "RNA", "PROTEINS")
     * @return The sample code that has been generated
     * @throws DataSetRegistrationException
     */
    String createNewTestSample(String parentBioSampleCode, String sampleType) throws DataSetRegistrationException

    /**
     * Creates a new analysis run sample, that
     * @param parentTestSampleCode
     * @return
     * @throws DataSetRegistrationException
     */
    String createNewAnalysisRunSample(String parentTestSampleCode) throws DataSetRegistrationException

    String determineSampleType(String sampleCode) throws ResourceAccessException

    OpenbisTestSample findTestSample(String sampleCode)

    String registerDataSet(List<URL> dataSetFiles, String runSampleCode) throws DataSetRegistrationException

}