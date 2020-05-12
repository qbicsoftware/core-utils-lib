package life.qbic.utils

import org.everit.json.schema.ValidationException
import spock.lang.Specification

import java.nio.file.NotDirectoryException
import java.nio.file.Paths
import java.text.ParseException

class NanoporeParserSpec extends Specification {

    def exampleDirectoriesRoot = this.getClass().getResource("/dummyFileSystem/nanopore-instrument-output").getPath()

    def "parsing a valid file structure creates a Map"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345")
        when:
        def map = NanoporeParser.parseFileStructure(pathToDirectory)
        then:
        assert map instanceof Map
    }

    def "parsing an invalid file structure throws ValidationError"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_entries/QABCD001AB_E12A345a01_PAE12345/20200122_1217_1-A1-B1-PAE12345_1234567a")
        when:
        NanoporeParser.parseFileStructure(pathToDirectory)
        then:
        thrown(ValidationException)
    }

    def "parsing an empty directory throws ParseException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/empty_directory/")
        when:
        NanoporeParser.parseFileStructure(pathToDirectory)
        then:
        thrown(ParseException)
    }

    def "parsing a non-existing directory throws FileNotFoundException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_directory")
        when:
        NanoporeParser.parseFileStructure(pathToDirectory)
        then:
        thrown(FileNotFoundException)
    }

    def "parsing a file throws NotDirectoryException "() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345/20200122_1217_1-A1-B1-PAE12345_1234567a/report_.pdf")
        when:
        NanoporeParser.parseFileStructure(pathToDirectory)
        then:
        thrown(NotDirectoryException)
    }

}
