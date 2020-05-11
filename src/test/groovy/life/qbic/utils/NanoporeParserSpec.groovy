package life.qbic.utils

import spock.lang.Specification

import java.nio.file.Paths

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

    def "parsing an empty directory throws NullPointerException"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "/empty_directory")
        when:
        NanoporeParser.parseFileStructure(pathToDirectory)
        then:
        thrown(NullPointerException)
    }

    def "parsing a non-existing directory throws IOError"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/missing_directory")
        when:
        NanoporeParser.parseFileStructure(pathToDirectory)
        then:
        thrown(IOException)
    }
}