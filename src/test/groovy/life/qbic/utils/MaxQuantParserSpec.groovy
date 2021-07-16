package life.qbic.utils

import life.qbic.datamodel.datasets.MaxQuantRunResult
import life.qbic.datasets.parsers.DataParserException
import life.qbic.datasets.parsers.DatasetValidationException
import spock.lang.Specification

import java.nio.file.Paths

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
        //TODO remove print
        println("#### Got $maxQuantRunResult for $pathToDirectory")
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

    def "parsing an invalid file structure throws DatasetValidationException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_combined_directory")
        when:
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        thrown(DatasetValidationException)
    }

    def "Missing files in txt directory throws DatasetValidationException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_file_in_txt_directory")
        when:
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        thrown(DatasetValidationException)
    }

    def "Missing root files throws DatasetValidationException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_root_files_directory")
        when:
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        thrown(DatasetValidationException)
    }

    def "parsing an empty directory throws DataParserException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "empty_directory/")
        //FIXME remove println
        println("Trying to create directory $pathToDirectory")
        // Maven doesn't include empty folders in build process so it has to be generated explicitly
        File directory = new File(pathToDirectory.toString())
        println("Directory $directory exists? ${directory.exists()}")
        if (!directory.exists()) {
            println("Creating directory $directory")
            directory.mkdir()
            println("Created directory $directory")
        }
        when:
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        DataParserException parseException = thrown(DataParserException)
        assert parseException.message == ("Specified directory ${pathToDirectory.toString()} is empty")
        // Remove new created folder after testing
        directory.delete()
    }

    def "parsing a non-existing directory throws DataParserException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_directory")
        when:
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        DataParserException parseException = thrown(DataParserException)
        assert parseException.message == ("The given path '${pathToDirectory.toString()}' does not exist.")
    }

    def "parsing a file throws DataParserException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/mqpar.xml")
        when:
        maxQuantParser.parseFrom(pathToDirectory)
        then:
        DataParserException parseException = thrown(DataParserException)
        assert parseException.message == ("Expected a directory. Got a file instead.")
    }
}
