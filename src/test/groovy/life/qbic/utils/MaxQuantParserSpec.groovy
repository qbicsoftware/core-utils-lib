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
        assert maxQuantRunResult.allPeptides.getRelativePath() == "./combined/txt/allPeptides.txt"
        assert maxQuantRunResult.allPeptides.getName()== "allPeptides.txt"

        assert maxQuantRunResult.evidence.getRelativePath() == "./combined/txt/evidence.txt"
        assert maxQuantRunResult.evidence.getName()== "evidence.txt"

        assert maxQuantRunResult.experimentalDesignTemplate.getRelativePath() == "./combined/txt/experimentalDesignTemplate.txt"
        assert maxQuantRunResult.experimentalDesignTemplate.getName()== "experimentalDesignTemplate.txt"

        assert maxQuantRunResult.parameters.getRelativePath() == "./combined/txt/parameters.txt"
        assert maxQuantRunResult.parameters.getName()== "parameters.txt"

        assert maxQuantRunResult.peptides.getRelativePath() == "./combined/txt/peptides.txt"
        assert maxQuantRunResult.peptides.getName()== "peptides.txt"

        assert maxQuantRunResult.proteinGroups.getRelativePath() == "./combined/txt/proteinGroups.txt"
        assert maxQuantRunResult.proteinGroups.getName()== "proteinGroups.txt"

        assert maxQuantRunResult.summary.getRelativePath() == "./combined/txt/summary_1234.pdf"
        assert maxQuantRunResult.summary.getName()== "summary_1234.pdf"
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
