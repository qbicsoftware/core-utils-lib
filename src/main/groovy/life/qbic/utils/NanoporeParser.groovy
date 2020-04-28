package life.qbic.utils

import groovy.util.logging.Log4j2

import java.nio.file.NotDirectoryException
import java.nio.file.Path

@Log4j2
class NanoporeParser {

    Path targetDirectoryPath

    private NanoporeParser() {
        throw new AssertionError()
    }

    NanoporeParser(Path targetDirectoryPath) {
        this.targetDirectoryPath = targetDirectoryPath
    }

    /**
     * Method where all the magic of the nanopore parser takes place
     * @param directory
     */
    def parseDirectory(Path directory) {
        // Step1: convert directory to json
        Map convertedDirectory = DirectoryConverter.fileTreeToMap(directory)
        // Step2: validate json
        // Step3: create data-model-lib objects from json
    }

    /**
     * Converts a file tree into a json object.
     */
    private class DirectoryConverter {

        /**
         *
         * @param path a path to the directory which will be used as root for parsing
         * @return a Map describing the file tree starting from the given path
         */
        static Map fileTreeToMap(Path path) {
            File rootLocation = new File(path.toString())
            if (rootLocation.isFile()) {
                log.error("Expected directory. Got file instead.")
                throw new NotDirectoryException("Expected a directory. Got a file instead.")
            } else {
                // Recursive conversion
                return convertDirectory(rootLocation.toPath())
            }

        }

        /**
         *
         * @param path
         * @return a map representing a directory with name, path and children as keys
         */
        private static Map convertDirectory(Path path) {
            // convert to File object
            File currentDirectory = new File(path.toString())
            String name = currentDirectory.getName()
            List children = currentDirectory.listFiles().collect {
                file ->
                    if (file.isFile()) {
                        convertFile(file.toPath())
                    } else if (file.isDirectory()) {
                        convertDirectory(file.toPath())
                    }
            }

            def convertedDirectory = [
                    "name"    : name,
                    "path"    : path,
                    "children": children
            ]

            return convertedDirectory
        }

        /**
         *
         * @param path
         * @return a map representing the file with name, path and file_type as keys
         */
        private static Map convertFile(Path path) {
            final predefinedExtensions = ["fastq.gz"]
            // convert to File object
            File currentFile = new File(path.toString())
            String name = currentFile.getName()
            // defaults to the string following the last '.' in the filename
            String fileType = name.tokenize('.').last()
            // check for predefined file type extensions
            for (extension in predefinedExtensions) {
                if (name.endsWith(extension)) fileType = extension
            }

            def convertedFile = [
                    "name"     : name,
                    "path"     : path,
                    "file_type": fileType
            ]
            return convertedFile
        }
    }
}
