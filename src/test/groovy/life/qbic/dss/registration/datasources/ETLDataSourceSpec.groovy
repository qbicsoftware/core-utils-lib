package life.qbic.dss.registration.datasources

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.SearchService
import life.qbic.dss.registration.usecases.exceptions.DataSetRegistrationException
import spock.lang.Shared
import spock.lang.Specification

/**
 * <add class description here>
 *
 * @author: Sven Fillinger
 */
class ETLDataSourceSpec extends Specification {

    @Shared
    private IDataSetRegistrationTransactionV2 transaction
    @Shared
    private ISample goodTestSample
    @Shared
    private SearchService goodSearchService

    def setupSpec() {
        goodTestSample = Stub(ISample)
        goodTestSample.getSampleType(_) >> "Q_TEST_SAMPLE"
        goodTestSample.getCode() >> "QTEST099HH"
        goodTestSample.getSpace() >> "Q_TEST"

        goodSearchService = Stub(SearchService)
        goodSearchService.searchForSamples(_) >> [goodTestSample]

        transaction = Stub(IDataSetRegistrationTransactionV2)
        transaction.createNewSample(*_) >> goodTestSample
        transaction.getSearchService() >> goodSearchService
        //transaction.createNewSample("EVIL_SAMPLE_CODE", "Q_TEST_SAMPLE") >> { throw new Exception("Mimic DSS error") }
    }

    def "Create a new test sample successfully"() {
        when:
        def source = new ETLDataSource(transaction)
        def testSampleCode = source.createNewTestSample("QUK17664GI", "DNA")

        then:
        assert testSampleCode.equals("QTEST099HH")
    }

    def "When an error occurs during new test sample creation, the system shall throw an DataSetRegistrationException"() {
        when:
        def source = new ETLDataSource(transaction)
        source.createNewTestSample("EVIL_SAMPLE_CODE", "DNA")

        then:
        thrown(DataSetRegistrationException)
    }

}
