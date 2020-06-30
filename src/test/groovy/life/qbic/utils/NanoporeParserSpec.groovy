package life.qbic.utils

import life.qbic.datamodel.datasets.OxfordNanoporeExperiment
import org.everit.json.schema.ValidationException
import spock.lang.Specification

import java.nio.file.NotDirectoryException
import java.nio.file.Paths
import java.text.ParseException

class NanoporeParserSpec extends Specification {

  def exampleDirectoriesRoot = this.getClass().getResource("/dummyFileSystem/nanopore-instrument-output").getPath()

  def "parsing a valid file structure returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109"
  }

  def "parsing a pooled file structure returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_pooled")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    def measurement = experiment.getMeasurements().get(0)
    def List<String> keys = new ArrayList<>(measurement.getRawDataPerSample(experiment).keySet())
    then:
    
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert measurement.getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert measurement.getLibraryPreparationKit() == "SQK-LSK109"
    // check that multiple pooled samples are contained
    assert keys.size() > 1
    // ...and that they are linked to two different folder structures
    assert measurement.getRawDataPerSample(experiment).get(keys.get(0)) != measurement.getRawDataPerSample(experiment).get(keys.get(1))
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
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "/empty_directory/")
    // Maven doesn't include empty folders in build process so it has to be generated explicitly
    File directory = new File(pathToDirectory.toString())
    if (!directory.exists()) {
      directory.mkdir()
    }
    when:
    NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    thrown(ParseException)
    // Remove new created folder after testing
    directory.delete()
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
