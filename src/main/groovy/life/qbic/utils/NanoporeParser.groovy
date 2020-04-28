package life.qbic.utils


import java.nio.file.Path

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
        // Step2: validate json
        // Step3: create data-model-lib objects from json
    }

    /**
     * Converts a file tree into a json object.
     */
    private class DirectoryConverter {

        private class JsonDirectory {
            String name
            Path path
            ArrayList children

            private JsonDirectory() {
                throw new AssertionError()
            }

            JsonDirectory(String name, Path path, List children) {
                this.name = name
                this.path = path
                this.children = children
            }
        }

        private class JsonFile {
            String name
            Path path
            String extension

            private JsonFile() {
                throw new AssertionError()
            }

            JsonFile(String name, Path path, String extension) {
                this.name = name
                this.path = path
                this.extension = extension
            }
        }

        /**
         *
         * @param path a path to the directory which will be used as root for parsing
         * @return a Map describing the file tree starting from the given path
         */
        static Map fileTreeToMap(Path path) {

        }
    }
}
