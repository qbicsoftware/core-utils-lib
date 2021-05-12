package life.qbic.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Log4j2
import life.qbic.datamodel.datasets.NfCorePipelineResult
import life.qbic.datasets.parsers.DataParserException
import life.qbic.datasets.parsers.DatasetParser
import life.qbic.datasets.parsers.DatasetValidationException

import java.nio.file.NotDirectoryException
import java.nio.file.Path
import java.text.ParseException


/**
 * <h1>Parser storing the filetree of a nf-core pipeline output directory into JSON format</h1>
 * <br>
 * <p>Converts a filetree generated by a nf-core pipeline into a JSON String similar to {@link NanoporeParser}
 *
 * @since 1.8.0
 * @param directory path of nf-core directory whose fileTree should be converted into a JSON String
 *
 */

@Log4j2
class BioinformaticAnalysisParser implements DatasetParser<NfCorePipelineResult>{

    /**
     * Possible product groups
     *
     * This enum describes the product groups into which the products of an offer are listed.
     *
     */
    enum RootFolderTypes {
        QUALITYCONTROL("qualityControl"),
        PIPELINEINFORMATION("pipelineInformation"),
        PROCESSFOLDERS("processFolders")
        private String name

        RootFolderTypes(String name) {
            this.name = name;
        }

        String getName() {
            return this.name;
        }
    }

    /**
     * Possible product groups
     *
     * This enum describes the product groups into which the products of an offer are listed.
     *
     */
    enum RootFileTypes {
        RUNID("runId"),
        SAMPLEID("sampleIds"),

        private String name

        RootFileTypes(String name) {
            this.name = name;
        }

        String getName() {
            return this.name;
        }
    }

    /**
     * Generates a map representing the folder structure
     * @param directory path of directory whose fileTree should be converted into map
     * @since 1.8.0
     */
    static String parseFileStructure(Path directory) {
        Map fileTreeMap = DirectoryConverter.fileTreeToMap(directory)
        String json = mapToJson(fileTreeMap)
        return json
    }

    /**
     * Method which converts a map into json String
     * @param map a nested map representing a fileTree structure
     */
    private static String mapToJson(Map map) {
        List<Map> rootChildren = map.get("children") as List<Map>
        List<Map> processFolders = []
        rootChildren.each {currentChild ->
            if ( currentChild.containsKey("children") ) {
                //folder
                switch(currentChild.get("name")) {
                    case "multiqc":
                        insertAsProperty(map, currentChild, RootFolderTypes.QUALITYCONTROL.getName())
                        break
                    case "pipeline_info":
                        parsePipelineInformation(currentChild)
                        insertAsProperty(map, currentChild, RootFolderTypes.PIPELINEINFORMATION.getName())
                        break
                    default:
                        processFolders.add(currentChild)
                        break
                }

            } else if ( currentChild.containsKey("file_type") ) {
                //file
                switch (currentChild.get("name")) {
                    case "run_id.txt":
                        insertAsProperty(map, currentChild, RootFileTypes.RUNID.name)
                        break
                    case "sample_ids.txt":
                        insertAsProperty(map, currentChild, RootFileTypes.SAMPLEID.name)
                        break
                    default:
                        //ignore other files
                        log.warn("Could not recognize file ${currentChild.path}")
                        break
                }

            }
        }
        insertAsProperty(map, processFolders, RootFolderTypes.PROCESSFOLDERS.name)

        ObjectMapper jsonMapper = new ObjectMapper()
        String json = jsonMapper.writeValueAsString(map)
        return json
    }

    /**
     * Modifies in place
     * @param pipelineInformation the folder containing the pipeline information
     * @return the folder containing the pipeline information with extracted properties as keys
     */
    private static void parsePipelineInformation(Map pipelineInformation) {

        pipelineInformation.get("children").each { Map child ->
            switch (child.get("name")) {
                case "software_versions":
                    String keyName = "softwareVersions"
                    insertAsProperty(pipelineInformation, child, keyName)
                    break
                case "execution_report.txt":
                    String keyName = "executionReport"
                    insertAsProperty(pipelineInformation, child, keyName)
                    break
                case "pipeline_report.txt":
                    String keyName = "pipelineReport"
                    insertAsProperty(pipelineInformation, child, keyName)
                    break
                default:
                    //ignoring other children
                    break
            }
        }
    }

    /**
     * Inserts a map content into the provided parent map using the propertyName as key
     * @param parent the map to be modified
     * @param content the content to be added under the new key
     * @param propertyName the newly added key
     * @since 1.8.0
     */
    private static void insertAsProperty(Map parent, Object content, String propertyName) {
        parent.put(propertyName,content)
    }

    /*
     * Converts a file tree into a json object.
     */
    private static class DirectoryConverter {

        /**
         *
         * @param path a path to the directory which will be used as root for parsing
         * @return a Map describing the file tree starting from the given path
         * @since 1.8.0
         * @throws FileNotFoundException in case the given directory does not exist
         * @throws IOException the input path could not be processed
         * @throws ParseException in case the directory is empty
         */
        static Map fileTreeToMap(Path path) throws FileNotFoundException, IOException, ParseException {
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
            //Since each value in the root map is a map we need to iterate over each key/value pair
                //ToDo Why does this not work?
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
                return path.replace("${root.toString()}/", "./")
            }
        }

        /**
         * File to JSON converter
         * @param a path to the current file in recursion
         * @return a map representing the file with name, path and file_type as keys
         */
        private static Map convertFile(Path path) {
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

    /**
     * Parses and validates a data structure from a given path of a directory in the filesystem.
     * <p> As an example, for a data set such as this:
     * <pre>
     * - /SomePath/MyDataset
     *      | - myFile.txt
     *      ` - anotherFile.txt
     * </pre>
     * you would need to provide <code>"/SomePath/MyDataset"</code> as path.</p>
     *
     * @param root The root path of the dataset structure, represents the top level of the
     * hierarchical data set structure. This path must be absolute.
     * @return A successfully parsed and validated dataset
     * @throws DataParserException if the data type cannot be parsed (unknown data type)
     * @throws DatasetValidationException if the data structure does not match a predefined schema
     * @since 1.7.0
     */
    @Override
    NfCorePipelineResult parseFrom(Path root) throws DataParserException, DatasetValidationException {
        String json = parseFileStructure(root)
        //TODO validate
        //TODO convert
        //TODO return
        throw new RuntimeException("Method not implemented.")
    }


}