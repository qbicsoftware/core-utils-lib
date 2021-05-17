package life.qbic.utils

import spock.lang.Specification
import java.nio.file.NotDirectoryException
import java.nio.file.Paths
import java.text.ParseException

/**
 *  Tests for the BioinformaticAnalysisParser
 *
 * @since 1.8.0
 * @see BioinformaticAnalysisParser
 *
 */
class BioInformaticAnalysisSpec extends Specification {

    def exampleDirectoriesRoot = this.getClass().getResource("/dummyFileSystem/bioinformatic-analysis-output").getPath()
    BioinformaticAnalysisParser bioinformaticAnalysisParser = new BioinformaticAnalysisParser()

    def "parsing a valid file structure returns a NfCorePipelineResult object"() {
        given: "A valid nf-core pipeline output data structure"
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates")
        when: "we parse this valid structure"
        bioinformaticAnalysisParser.parseFrom(pathToDirectory)
        then: "we expect no exception should be thrown"
        noExceptionThrown()
    }

    def "parsing an empty directory throws ParseException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "empty_directory/")
        // Maven doesn't include empty folders in build process so it has to be generated explicitly
        File directory = new File(pathToDirectory.toString())
        if (!directory.exists()) {
            directory.mkdir()
        }
        when:
        bioinformaticAnalysisParser.parseFrom(pathToDirectory)
        then:
        thrown(ParseException)
        // Remove new created folder after testing
        directory.delete()
    }

    def "parsing a non-existing directory throws FileNotFoundException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_directory")
        when:
        bioinformaticAnalysisParser.parseFrom(pathToDirectory)
        then:
        thrown(FileNotFoundException)
    }

    def "parsing a file throws NotDirectoryException "() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/pipeline_info/execution_report.txt")
        when:
        bioinformaticAnalysisParser.parseFrom(pathToDirectory)
        then:
        thrown(NotDirectoryException)
    }
}
