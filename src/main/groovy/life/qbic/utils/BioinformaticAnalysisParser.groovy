package life.qbic.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Log4j2
import java.nio.file.NotDirectoryException
import java.nio.file.Path
import java.text.ParseException


/**
 * <class short description - One Line!>
 *
 * <More detailed description - When to use, what it solves, etc.>
 *
 * @since: <versiontag>
 *
 */
@Log4j2
class BioinformaticAnalysisParser {

    /**
     * Generates a map representing the folder structure
     * @param directory path of directory whose fileTree should be converted into map
     */
    static String parseFileStructure(Path directory) {
        // Step1: convert directory to json
        Map convertedDirectory = DirectoryConverter.fileTreeToMap(directory)
        String json = mapToJson(convertedDirectory)
        return json
    }

    /**
     * Method which converts a map into json String
     * @param map a nested map representing a fileTree structure
     */
    private static String mapToJson(Map map) {
        ObjectMapper jsonMapper = new ObjectMapper()
        String json = jsonMapper.writeValueAsString(map)
        return json
    }

    /*
     * Converts a file tree into a json object.
     */
    private static class DirectoryConverter {

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
            } else if (rootLocation.isDirectory()) {
                //Check if existing Directory is empty
                if (rootLocation.list().length > 0) {
                    // Recursive conversion
                    Map folderStructure = convertDirectory(rootLocation.toPath())
                    return convertToRelativePaths(folderStructure, rootLocation.toPath())
                } else {
                    log.error("Specified directory is empty")
                    throw new ParseException("Parsed directory might not be empty", -1)
                }
            } else {
                if (!rootLocation.exists()) {
                    log.error("The given directory does not exist.")
                    throw new FileNotFoundException("The given path does not exist.")
                } else {
                    log.error("Input path could not be processed")
                    throw new IOException()
                }
            }

        }

        /**
         * Convert a directory structure to a map, following the BioinformaticAnalysis schema.
         * @param a path to the current location in recursion
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

        private static Map convertToRelativePaths(Map content, Path root) {
            content["path"] = toRelativePath(content["path"] as String, root)
            if (content["children"]) {
                // Children always contains a map, so convert recursively
                content["children"] = (content["children"] as List).collect { convertToRelativePaths(it as Map, root) }
            }
            return content

        }

        private static String toRelativePath(String path, Path root) {
            if (root.toString().equals(path)) {
                return "./"
            } else {
                return path.replace("${root.toString()}/", "")
            }
        }

        /**
         * File to JSON converter
         * @param a path to the current file in recursion
         * @return a map representing the file with name, path and file_type as keys
         */
        private static Map convertFile(Path path) {
            // convert to File object
            File currentFile = new File(path.toString())
            String name = currentFile.getName()
            String fileType = determineFileType(name)


            def convertedFile = [
                    "name"     : name,
                    "path"     : path,
                    "file_type": fileType
            ]
            return convertedFile
        }

        /**
         * This method extracts the file type also called extension from the filename.
         * The type defaults to the substring after the last `.` character in the string.
         * @param fileName the full name of the file including extension
         * @return the extension of the filename that was provided
         */
        private static String determineFileType(String fileName) {
            // defaults to the string following the last '.' in the filename
            String fileType = fileName.tokenize('.').last()

            return fileType
        }

    }

}
