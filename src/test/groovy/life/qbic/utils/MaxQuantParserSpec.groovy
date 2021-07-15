package life.qbic.utils

import life.qbic.datamodel.datasets.MaxQuantRunResult
import org.everit.json.schema.ValidationException
import spock.lang.Specification

import java.nio.file.NotDirectoryException
import java.nio.file.Paths
import java.text.ParseException

/**
 *  Tests for the MaxQuantParser
 *
 * @since 1.9.0
 * @see MaxQuantParser
 *
 */
class MaxQuantParserSpec extends Specification {

    def exampleDirectoriesRoot = this.getClass().getResource("/dummyFileSystem/maxquant-run-output").getPath()
    MaxQuantParser maxQuantParser = new MaxQuantParser()

    def "parsing a valid file structure returns a maxQuantRunResult object"() {
        given: "A valid maxQuant run output data structure"
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates")
        when: "we parse this valid structure"
        MaxQuantRunResult maxQuantRunResult = maxQuantParser.parseFrom(pathToDirectory)
        then: "we expect no exception should be thrown"
        assert maxQuantRunResult instanceof MaxQuantRunResult
        //Root files can be parsed
        assert maxQuantRunResult.runParameters.getRelativePath() == "./mqpar.xml"
        assert maxQuantRunResult.runParameters.getName()== "mqpar.xml"

        assert maxQuantRunResult.sampleIds.getRelativePath() == "./sample_ids.txt"
        assert maxQuantRunResult.sampleIds.getName()== "sample_ids.txt"

        //Files in ./combined/txt/ can be parsed
        assert maxQuantRunResult.allPeptides.getRelativePath() == "./mqpar.xml"
        assert maxQuantRunResult.allPeptides.getName()== "mqpar.xml"

        assert maxQuantRunResult.evidence.getRelativePath() == "./sample_ids.txt"
        assert maxQuantRunResult.evidence.getName()== "sample_ids.txt"

        assert maxQuantRunResult.experimentalDesignTemplate.getRelativePath() == "./mqpar.xml"
        assert maxQuantRunResult.experimentalDesignTemplate.getName()== "mqpar.xml"

        assert maxQuantRunResult.parameters.getRelativePath() == "./sample_ids.txt"
        assert maxQuantRunResult.parameters.getName()== "sample_ids.txt"

        assert maxQuantRunResult.peptides.getRelativePath() == "./mqpar.xml"
        assert maxQuantRunResult.peptides.getName()== "mqpar.xml"

        assert maxQuantRunResult.proteinGroups.getRelativePath() == "./sample_ids.txt"
        assert maxQuantRunResult.proteinGroups.getName()== "sample_ids.txt"

        assert maxQuantRunResult.summary.getRelativePath() == "./sample_ids.txt"
        assert maxQuantRunResult.summary.getName()== "sample_ids.txt"
    }

    def "parsing an invalid file structure throws ValidationError"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails")
        when:
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        thrown(ValidationException)
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
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        ParseException parseException = thrown(ParseException)
        assert parseException.message.equals("Specified directory is empty")
        // Remove new created folder after testing
        directory.delete()
    }

    def "parsing a non-existing directory throws FileNotFoundException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_directory")
        when:
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        thrown(FileNotFoundException)
    }

    def "parsing a file throws NotDirectoryException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/mqpar.xml")
        when:
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        thrown(NotDirectoryException)
    }
}
