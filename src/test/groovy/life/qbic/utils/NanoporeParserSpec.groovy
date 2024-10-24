package life.qbic.utils

import life.qbic.datamodel.datasets.OxfordNanoporeExperiment
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

  def "parsing a valid file structure with an html report returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_html_report")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109"
  }

  def "parsing the alternative valid file structure returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_new")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }

  def "parsing the newest valid file structure returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_v3")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }

  def "parsing the a valid file structure with a second basecalling folder returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_v4")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }

  def "parsing the a valid file structure v4 without basecalling folder returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_no_basecalling_v4")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }

  def "parsing a valid minimal file structure containing additional unknown files and folder still returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_valid_minimal")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }

  def "parsing a valid minimal pooled file structure containing additional unknown files and folder still returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_valid_minimal_pooled")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }
  /* Schema Validation has been deprecated since the nanopore schema changes too much to be handled

  def "parsing an invalid minimal file structure leads to a ValidationException"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/QABCD001AB_E12A345a01_PAE12345_missing_minimal_information")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    thrown(ValidationException)
  }
  */

  def "parsing a valid minimal file structure for dorado based basecalling containing additional unknown files and folder still returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_valid_dorado_minimal")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }

  def "parsing a valid minimal file structure with bam files and dorado basecalling returns an OxfordNanoporeExperiment"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_minimal_bam")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }

  def "parsing a valid minimal file structure with pod5 files and dorado basecalling returns an OxfordNanoporeExperiment"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_minimal_pod5")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }

  def "parsing a valid file structure for dorado based basecalling containing additional unknown files and folder still returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_valid_dorado_example")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    //assert experiment.getMeasurements().get(0).getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
  }

   /*Schema Validation has been deprecated since the nanopore schema changes too much to be handled
  def "parsing an invalid minimal file structure for dorado based basecalling leads to a ValidationException"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/QABCD001AB_E12A345a01_PAE12345_missing_skip_folder")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    thrown(ValidationException)
  }
  */
  def "parsing the alternative valid file structure with metadata missing returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_nanopore_new_minimal")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file is parsed
    assert experiment.getMeasurements().get(0).getFlowCellType() == "FLO-PRO002"
    // Check that the metadata from the summary file has been retrieved, but data can also be empty
    assert experiment.getMeasurements().get(0).getLibraryPreparationKit() == "SQK-LSK109-XL"
    assert experiment.getMeasurements().get(0).getFlowcellId() == "flow_cell_from_summary"
    assert experiment.getMeasurements().get(0).getAsicTemp() == ""
    assert experiment.getMeasurements().get(0).getMachineHost() == ""
    assert experiment.getMeasurements().get(0).getStartDate() == ""
  }

  def "qc folder is ignored"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/with_qc_folder/QABCD001AB_E12A345a01_PAE12345")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    then:
    assert experiment instanceof OxfordNanoporeExperiment
  }

  def "parsing a pooled file structure returns an OxfordNanoporeExperiment Object"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345_pooled")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)
    def measurement = experiment.getMeasurements().get(0)
    def rawDataPerSample = new ArrayList<>(measurement.getRawDataPerSample(experiment).entrySet())
    then:

    assert experiment instanceof OxfordNanoporeExperiment
    // Check that the metadata from the report file has been retrieved
    assert measurement.getMachineHost() == "PCT0094"
    // Check that the metadata from the summary file has been retrieved
    assert measurement.getLibraryPreparationKit() == "SQK-LSK109"
    // check that multiple pooled samples are contained
    assert rawDataPerSample.size() > 1
    // ...and that they are linked to two different folder structures
    assert rawDataPerSample.get(0) != rawDataPerSample.get(1)
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

  def "missing base caller information shall throw a Runtime Exception"() {
    given:
    def pathToDirectory = Paths.get(exampleDirectoriesRoot,
        "fails/QABCD001AB_E12A345a01_PAE12345_missing_metadata")
    when:
    def experiment = NanoporeParser.parseFileStructure(pathToDirectory)

    then:
    thrown(RuntimeException)

  }
}
