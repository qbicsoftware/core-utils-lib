package life.qbic.dss.registration.datasources

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria
import life.qbic.datamodel.identifiers.SampleCodeFunctions
import life.qbic.datamodel.samples.OpenbisTestSample
import life.qbic.dss.registration.usecases.DataSetRegistrationDataSource
import life.qbic.dss.registration.usecases.exceptions.DataSetRegistrationException
import life.qbic.dss.registration.usecases.exceptions.SampleCreationException

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

    private static final String REGISTER_SAMPLE_TYPE = "Q_TEST_SAMPLE"

    ETLDataSource(IDataSetRegistrationTransactionV2 transaction) {
        this.transaction = transaction
    }

    @Override
    String createNewTestSample(String parentBioSampleCode, String type) throws SampleCreationException {
        if (!SampleCodeFunctions.isQbicBarcode(parentBioSampleCode)) {
            throw new SampleCreationException("Provided sample ${parentBioSampleCode} code is not a valid QBiC sample code.")
        }
        def projectCode = SampleCodeFunctions.getProjectPrefix(parentBioSampleCode)
        def availableSampleCode = determineNextFreeSampleCode(projectCode)
        def projectSpace = determineProjectSpace(parentBioSampleCode)
        def newRegisteredSample = this.transaction.createNewSample(
                createIdentifer(projectSpace, availableSampleCode),
                REGISTER_SAMPLE_TYPE)
        newRegisteredSample.setParentSampleIdentifiers([createIdentifer(projectSpace, parentBioSampleCode)])
        return newRegisteredSample.getCode()
    }

    private static String createIdentifer(String space, String code) {
        return "/$space/$code"
    }

    private String determineProjectSpace(String sampleCode) {
        def searchService = transaction.getSearchService()
        def searchCriteria = new SearchCriteria()
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.PROJECT, sampleCode))

        def foundSamples = searchService.searchForSamples(searchCriteria)
        if (foundSamples.isEmpty()) {
            throw new SampleCreationException("Could not determine space for sample ")
        }
        return foundSamples[0].getSpace()
    }

    private String determineNextFreeSampleCode(String projectCode) {
        def samples = findAllTestSamples(projectCode)
        def base = projectCode + "001A"
        def firstFreeBarcode = base + SampleCodeFunctions.checksum(base)
        samples.each { sample ->
            String code = sample.getCode()
            if (SampleCodeFunctions.isQbicBarcode(code)) {
                if (SampleCodeFunctions.compareSampleCodes(firstFreeBarcode, code) <= 0) {
                    firstFreeBarcode = SampleCodeFunctions.incrementSampleCode(code)
                }
            }
        }
    }

    private List<ISampleImmutable> findAllTestSamples(String projectCode) {
        final def searchCriteria = new SearchCriteria()
        final def searchService = transaction.getSearchService()
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createPropertyMatch("Project", projectCode))
        List<ISampleImmutable> samples = searchService.searchForSamples(searchCriteria)
        return samples
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
