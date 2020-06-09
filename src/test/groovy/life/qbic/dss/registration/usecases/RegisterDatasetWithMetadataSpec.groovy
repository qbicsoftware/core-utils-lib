package life.qbic.dss.registration.usecases

import life.qbic.dss.registration.usecases.dtos.ExperimentType
import life.qbic.dss.registration.usecases.dtos.UploadMetaData
import life.qbic.dss.registration.usecases.dtos.UploadSample
import life.qbic.dss.registration.usecases.exceptions.DataSetRegistrationException
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

/**
 * <add class description here>
 *
 * @author: Sven Fillinger
 */
class RegisterDatasetWithMetadataSpec extends Specification {

    @Shared
    private UploadMetaData goodUploadMetaData
    @Shared
    private UploadMetaData evilUploadMetaData
    @Shared
    private List<URL> goodDataSet
    @Shared
    private UploadSample goodUploadSample
    @Shared
    private DataSetRegistrationOutput dummyOutput

    def setupSpec() {
        dummyOutput = Mock(DataSetRegistrationOutput)
        goodUploadSample = new UploadSample("GS00001", "QTEST001AE")
        goodDataSet = new ArrayList<>()
        goodDataSet.add(Thread.currentThread().getContextClassLoader().getResource("dss-registration/example.file.1.fastq"))
        goodDataSet.add(Thread.currentThread().getContextClassLoader().getResource("dss-registration/example.file.2.fastq"))
        goodUploadMetaData = new UploadMetaData(goodDataSet, ExperimentType.DNA_SEQ, goodUploadSample)
    }

    def "If sample code cannot be matched to either a biological sample or a test sample, invoke output error"() {
        given:
        DataSetRegistrationDataSource dataSource = Stub(DataSetRegistrationDataSource)
        dataSource.registerDataSet(*_) >> { throw new DataSetRegistrationException("Test") }

        when:
        RegisterDatasetWithMetadata registerDatasetWithMetadata = new RegisterDatasetWithMetadata(dataSource, dummyOutput)
        registerDatasetWithMetadata.registerDataSet(goodUploadMetaData, goodUploadSample)

        then:
        1 * dummyOutput.invokeOnError(_)
    }
}
