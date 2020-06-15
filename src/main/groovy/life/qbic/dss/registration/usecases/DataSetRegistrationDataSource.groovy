package life.qbic.dss.registration.usecases

import life.qbic.datamodel.samples.OpenbisTestSample
import life.qbic.dss.registration.usecases.exceptions.DataSetRegistrationException
import life.qbic.dss.registration.usecases.exceptions.ResourceAccessException
import life.qbic.dss.registration.usecases.exceptions.SampleCreationException


interface DataSetRegistrationDataSource {

    /**
     * Creates a new test sample with an auto-generated sample code of a given sample type and attaches it to another sample as child
     * @param parentSampleCode
     * @param sampleType: The sample type (for example "DNA", "RNA", "PROTEINS")
     * @return The sample code that has been generated
     * @throws DataSetRegistrationException
     */
    String createAnalyteSample(String parentBioSampleCode, String sampleType) throws SampleCreationException

    /**
     * Creates a new analysis run sample, that
     * @param parentTestSampleCode
     * @return
     * @throws DataSetRegistrationException
     */
    String createAnalysisRunSample(String parentTestSampleCode) throws SampleCreationException

    /**
     * Returns the sample type for a given sample code
     *
     * The sample type must be one of:
     *
     * ["Source Sample", "Extract Sample", "Analyte Sample", "Analysis Run Sample"]
     *
     * @param sampleCode
     * @return
     * @throws ResourceAccessException
     */
    String determineSampleType(String sampleCode) throws ResourceAccessException

    Map findAnalyteSample(String sampleCode)

    String registerDataSet(List<URL> dataSetFiles, String runSampleCode) throws DataSetRegistrationException

}