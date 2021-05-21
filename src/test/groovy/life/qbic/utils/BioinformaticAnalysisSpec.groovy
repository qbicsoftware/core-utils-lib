package life.qbic.utils

import life.qbic.datamodel.datasets.NfCorePipelineResult
import life.qbic.datamodel.datasets.datastructure.files.DataFile
import life.qbic.datamodel.datasets.datastructure.folders.DataFolder
import life.qbic.datamodel.datasets.datastructure.folders.nfcore.PipelineInformationFolder
import life.qbic.datamodel.datasets.datastructure.folders.nfcore.QualityControlFolder
import org.everit.json.schema.ValidationException
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
        NfCorePipelineResult nfCorePipelineResult = bioinformaticAnalysisParser.parseFrom(pathToDirectory)
        println(nfCorePipelineResult)
        then: "we expect no exception should be thrown"
        assert nfCorePipelineResult instanceof NfCorePipelineResult
        //Root files can be parsed
        assert nfCorePipelineResult.runId.getRelativePath() == "./run_id.txt"
        assert nfCorePipelineResult.runId.getName()== "run_id.txt"
        assert nfCorePipelineResult.sampleIds.getRelativePath() == "./sample_ids.txt"
        assert nfCorePipelineResult.sampleIds.getName()== "sample_ids.txt"
        //Root Folder can be parsed
        QualityControlFolder multiQc = nfCorePipelineResult.getQualityControlFolder()
        assert multiQc.getRelativePath() == "./multiqc"
        assert multiQc.getName() == "multiqc"
        assert multiQc instanceof DataFolder

        PipelineInformationFolder pipelineInfo = nfCorePipelineResult.getPipelineInformation()
        assert pipelineInfo.getRelativePath() == "./pipeline_info"
        assert pipelineInfo.getName() == "pipeline_info"
        assert pipelineInfo instanceof DataFolder

        List<DataFolder> processFolders = nfCorePipelineResult.getProcessFolders()
        assert processFolders[0].getRelativePath()== "./salmon"
        assert processFolders[0].getName() == "salmon"
        assert processFolders[0] instanceof DataFolder

        //Childrens of Root folders can be parsed
        assert multiQc.getChildren()[0].getChildren()[0].getRelativePath() == "./multiqc/star_salmon/multiqc_report.html"
        assert multiQc.getChildren()[0].getChildren()[0].getName() == "multiqc_report.html"

        assert pipelineInfo.getChildren()[0].getRelativePath() == "./pipeline_info/software_versions.csv"
        assert pipelineInfo.getChildren()[0].getName() == "software_versions.csv"
        assert pipelineInfo.getChildren()[0] instanceof DataFile

        assert pipelineInfo.getChildren()[1].getRelativePath() == "./pipeline_info/execution_report.txt"
        assert pipelineInfo.getChildren()[1].getName() == "execution_report.txt"
        assert pipelineInfo.getChildren()[1] instanceof DataFile

        assert pipelineInfo.getChildren()[2].getRelativePath() == "./pipeline_info/pipeline_report.txt"
        assert pipelineInfo.getChildren()[2].getName() == "pipeline_report.txt"
        assert pipelineInfo.getChildren()[2] instanceof DataFile

    }

    def "parsing an invalid file structure throws ValidationError"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails")
        when:
        bioinformaticAnalysisParser.parseFrom(pathToDirectory)
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
        bioinformaticAnalysisParser.parseFrom(pathToDirectory)
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
