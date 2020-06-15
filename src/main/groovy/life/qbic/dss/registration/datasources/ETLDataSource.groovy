package life.qbic.dss.registration.datasources

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria
import life.qbic.datamodel.identifiers.SampleCodeFunctions
import life.qbic.datamodel.samples.OpenbisTestSample
import life.qbic.dss.registration.usecases.DataSetRegistrationDataSource
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

    private static final String EXTRACT_SAMPLE_TYPE = "Q_TEST_SAMPLE"

    ETLDataSource(IDataSetRegistrationTransactionV2 transaction) {
        this.transaction = transaction
    }

    @Override
    String createNewExtractSample(String parentBioSampleCode, String sampleType) throws SampleCreationException {
        if (!SampleCodeFunctions.isQbicBarcode(parentBioSampleCode)) {
            throw new SampleCreationException("Provided sample ${parentBioSampleCode} code is not a valid QBiC sample code.")
        }
        def projectCode = SampleCodeFunctions.getProjectPrefix(parentBioSampleCode)
        def availableSampleCode = determineNextFreeSampleCode(projectCode)
        def projectSpace = determineProjectSpace(parentBioSampleCode)
        def newRegisteredSample = this.transaction.createNewSample(
                createIdentifer(projectSpace, availableSampleCode),
                EXTRACT_SAMPLE_TYPE)

        newRegisteredSample.setParentSampleIdentifiers([createIdentifer(projectSpace, parentBioSampleCode)])
        newRegisteredSample.setExperiment(createSamplePreparationExperiment(projectSpace, projectCode, sampleType))
        return newRegisteredSample.getCode()
    }

    private IExperimentImmutable createSamplePreparationExperiment(String space, String projectCode, String sampleType) {
        def testSamplesWithType = findAllTestSamples(projectCode).findAll {
                it.getPropertyValue("Q_SAMPLE_TYPE").equals(sampleType)
        }
        if (testSamplesWithType){
            return testSamplesWithType[0].getExperiment()
        }
        def usedExperimentIdentifiers = [] as Set
        def searchService = transaction.getSearchService()
        def existingExperiments = searchService.listExperiments("/" + space + "/" + projectCode)
        def lastExperimentNumber = (existingExperiments as Set).size()

        def expExists = true
        def newExpID = ""
        while (expExists) {
            lastExperimentNumber += 1
            newExpID = "/$space/$projectCode/${projectCode}E$lastExperimentNumber"
            expExists = newExpID in usedExperimentIdentifiers
            usedExperimentIdentifiers.add(newExpID)
        }
        transaction.createNewExperiment(newExpID, "Q_SAMPLE_PREPARATION")
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
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.PROJECT, projectCode))
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createPropertyMatch("Q_SAMPLE_TYPE", EXTRACT_SAMPLE_TYPE))
        List<ISampleImmutable> samples = searchService.searchForSamples(searchCriteria)
        return samples
    }

    @Override
    String createNewAnalysisRunSample(String parentTestSampleCode) {
        return null
    }

    @Override
    String determineSampleType(String sampleCode) {
        final def searchCriteria = new SearchCriteria()
        final def searchService = transaction.getSearchService()
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleCode))
        def searchResult = searchService.searchForSamples(searchCriteria)
    }

    @Override
    OpenbisTestSample findExtractSample(String sampleCode) {
        return null
    }

    @Override
    String registerDataSet(List<URL> dataSetFiles, String runSampleCode) {
        return null
    }
}
