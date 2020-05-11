package life.qbic.utils

import spock.lang.Specification

import java.nio.file.Paths

class NanoporeParserSpec extends Specification {

    def exampleDirectoriesRoot = this.getClass().getResource("/dummyFileSystem/nanopore-instrument-output").getPath()

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
