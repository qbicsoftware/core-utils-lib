package life.qbic.utils

import spock.lang.Specification
import java.nio.file.NotDirectoryException
import java.nio.file.Paths
import java.text.ParseException

/**
 *  Tests for the BioinformaticAnalysisParser
 *
 * @since: 1.8.0
 * @see BioinformaticAnalysisParser
 *
 */
class BioInformaticAnalysisSpec extends Specification {

        def exampleDirectoriesRoot = this.getClass().getResource("/dummyFileSystem/bioinformatic-analysis-output").getPath()

        def "parsing a valid file structure returns a Json String"() {
            given:
            def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/resultset")
            when:
            def analysis = BioinformaticAnalysisParser.parseFileStructure(pathToDirectory)
            then:
            assert analysis instanceof String
            println(analysis)

        }

        def "parsing an empty directory throws ParseException"() {
            given:
            def pathToDirectory = Paths.get(exampleDirectoriesRoot, "/empty_directory/")
            // Maven doesn't include empty folders in build process so it has to be generated explicitly
            File directory = new File(pathToDirectory.toString())
            if (!directory.exists()) {
                directory.mkdir()
            }
            when:
            BioinformaticAnalysisParser.parseFileStructure(pathToDirectory)
            then:
            thrown(ParseException)
            // Remove new created folder after testing
            directory.delete()
        }

        def "parsing a non-existing directory throws FileNotFoundException"() {
            given:
            def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_directory")
            when:
            BioinformaticAnalysisParser.parseFileStructure(pathToDirectory)
            then:
            thrown(FileNotFoundException)
        }

        def "parsing a file throws NotDirectoryException "() {
            given:
            def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/resultset/pipeline_info/execution_report.txt")
            when:
            BioinformaticAnalysisParser.parseFileStructure(pathToDirectory)
            then:
            thrown(NotDirectoryException)
        }
}
