package life.qbic.dss.registration.datasources

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2
import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample

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

    def setupSpec() {
        goodTestSample = Stub(ISample)
        goodTestSample.getSampleType(_) >> "Q_TEST_SAMPLE"
        transaction = Stub(IDataSetRegistrationTransactionV2)
    }

    def "Create a new test sample successfully"() {

        when:
        def source = new ETLDataSource(transaction)
        def testSampleCode = source.createNewTestSample("QTEST001AE", "DNA")

        then:
        assert testSampleCode.equals("Q_TEST_SAMPLE")
    }

    def "When an error occurs during new test sample creation, the system shall throw an exception"() {

    }

}
