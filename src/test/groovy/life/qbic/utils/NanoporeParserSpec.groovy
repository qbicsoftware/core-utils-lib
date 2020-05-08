package life.qbic.utils

import spock.lang.Specification

import java.nio.file.Paths

class NanoporeParserSpec extends Specification {

    def exampleDirectoriesRoot = this.getClass().getResource("/dummyFileSystem/nanopore-instrument-output").getPath()

    def "parsing a valid file structure creates a Map"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "validates/QABCD001AB_E12A345a01_PAE12345")
        print(pathToDirectory)
        when:
        def map = NanoporeParser.parseFileStructure(pathToDirectory)
        then:
        map instanceof Map
    }

    def "parsing an empty directory returns null"() {
        given:
        def pathToDirectory = Paths.get(exampleDirectoriesRoot, "fails/empty_directory")
        when:
        def map = NanoporeParser.parseFileStructure(pathToDirectory)
        then:
        map == null
    }
}
