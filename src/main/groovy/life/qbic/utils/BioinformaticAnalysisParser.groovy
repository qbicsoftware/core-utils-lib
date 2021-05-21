package life.qbic.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Log4j2
import life.qbic.datamodel.datasets.NfCorePipelineResult
import life.qbic.datamodel.pipelines.PipelineOutput
import life.qbic.datasets.parsers.DataParserException
import life.qbic.datasets.parsers.DatasetParser
import life.qbic.datasets.parsers.DatasetValidationException
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaClient
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener

import java.nio.file.NotDirectoryException
import java.nio.file.Path
import java.text.ParseException


/**
 * <h1>Parser storing the fileTree of a nf-core pipeline output directory into JSON format</h1>
 * <br>
 * <p>Converts a fileTree generated by a nf-core pipeline into a JSON String similar to {@link NanoporeParser}
 *
 * @since 1.8.0
 * @param directory path of nf-core directory whose fileTree should be converted into a JSON String
 *
 */
@Log4j2
class BioinformaticAnalysisParser implements DatasetParser<NfCorePipelineResult>{

    /**
     * Contains the associated keys of the required root directory subFolders
     *
     * This enum contains the keys which are associated with the required subFolders of the root directory in the provided fileTree
     * @since 1.8.0
     */
    enum RequiredRootFolderKeys {
        QUALITY_CONTROL("qualityControl"),
        PIPELINE_INFORMATION("pipelineInformation"),
        PROCESS_FOLDERS("processFolders")

        private String keyName

        RequiredRootFolderKeys(String keyName) {
            this.keyName = keyName
        }

        String getKeyName() {
            return this.keyName
        }
    }

    /**
     * Contains the associated keys of the required files in the root directory
     *
     * This enum contains the keys which are associated with the required files of the root directory in the provided fileTree
     * @since 1.8.0
     */
    enum RequiredRootFileKeys {
        RUN_ID("runId"),
        SAMPLE_ID("sampleIds"),

        private String keyName

        RequiredRootFileKeys(String keyName) {
            this.keyName = keyName
        }

        String getKeyName() {
            return this.keyName
        }
    }

    /**
     * Contains the associated keys of the required files in the pipe_line directory
     *
     * This enum contains the keys which are associated with the required files in the pipeline directory of the provided fileTree
     * @since 1.8.0
     */
    enum RequiredPipelineFileKeys {
        SOFTWARE_VERSIONS("softwareVersions"),
        EXECUTION_REPORT("executionReport"),
        PIPELINE_REPORT("pipelineReport")

        private String keyName

        RequiredPipelineFileKeys(String keyName) {
            this.keyName = keyName
        }

        String getKeyName() {
            return this.keyName
        }

    }


    /** {@InheritDoc} */
    @Override
    NfCorePipelineResult parseFrom(Path root) throws DataParserException, DatasetValidationException {
        Map fileTreeMap = parseFileStructureToMap(root)
        adaptMapToDatasetStructure(fileTreeMap)
        try {
        String json = mapToJson(fileTreeMap)
        validateJson(json)
        NfCorePipelineResult nfCorePipelineResult = NfCorePipelineResult.createFrom(fileTreeMap)
        return nfCorePipelineResult
    }catch (ValidationException validationException) {
            log.error("Specified directory could not be validated")
            // we have to fetch all validation exceptions
            def causes = validationException.getAllMessages().collect{ it }.join("\n")
            log.error(causes)
            throw validationException
        }
    }

    /**
     * Generates a map representing the provided folder structure
     * @param directory path of directory whose fileTree should be converted into map
     * @since 1.8.0
     */
    private static Map parseFileStructureToMap(Path directory) {
        Map fileTreeMap = DirectoryConverter.fileTreeToMap(directory)
        return fileTreeMap
    }

    /**
     * Method which adapts the parsed map of the root directory in place to the expected file structure.
     * @see {<a href="https://github.com/qbicsoftware/data-model-lib/blob/master/src/test/resources/examples/resultset/valid-resultset-example.json">valid datastructure example</a>
     *
     * After parsing all directories and files of the provided filestructure are contained in the children property of the root directory map entry.
     * The underlying datastructure however expects a mapping of the expected folders and files to specific keys in the map outside of the children property such as
     * QualityControl: { "name": "multiqc",
     *                   "path": "./multiqc",
     *                   "children": []
     *                 }
     * @param map a nested map representing the parsed fileTree structure
     * @since 1.8.0
     */
    private static void adaptMapToDatasetStructure(Map map) {
        List<Map> rootChildren = map.get("children") as List<Map>
        List<Map> processFolders = []
        rootChildren.each { currentChild ->
            if (currentChild.containsKey("children")) {
                //folder
                String folderName = currentChild.get("name")
                switch (folderName) {
                    case "multiqc":
                        insertAsProperty(map, currentChild, RequiredRootFolderKeys.QUALITY_CONTROL.getKeyName())
                        break
                    case "pipeline_info":
                        parsePipelineInformation(currentChild)
                        insertAsProperty(map, currentChild, RequiredRootFolderKeys.PIPELINE_INFORMATION.getKeyName())
                        break
                    default:
                        processFolders.add(currentChild)
                        break
                }
            } else if (currentChild.containsKey("fileType")) {
                //file
                switch (currentChild.get("name")) {
                    case "run_id.txt":
                        insertAsProperty(map, currentChild, RequiredRootFileKeys.RUN_ID.getKeyName())
                        break
                    case "sample_ids.txt":
                        insertAsProperty(map, currentChild, RequiredRootFileKeys.SAMPLE_ID.getKeyName())
                        break
                    default:
                        //ignore other files
                        log.warn("Could not recognize file ${currentChild.path}")
                        break
                }
            }
        }
        insertAsProperty(map, processFolders, RequiredRootFolderKeys.PROCESS_FOLDERS.getKeyName())
    }

    /**
     * Method which adapts the parsed content of the pipeline_info directory in place to the expected file structure.
     * @see {<a href="https://github.com/qbicsoftware/data-model-lib/blob/master/src/test/resources/examples/resultset/valid-resultset-example.json">valid datastructure example</a>
     *
     * After parsing the files of the pipeline_info directory are contained in in the children property of the root directory.
     * The underlying datastructure however expects a mapping of the expected folders and files to a dedicated pipelineInformation Map entry containing the individual files as properties such as:
     * {
     *   "pipelineInformation": {
     *     "name": "pipeline_info",
     *     "path": "./pipeline_info",
     *     "children": [],
     *     "softwareVersions": {
     *       "name": "software_versions.csv",
     *       "fileType": "csv",
     *       "path": "./pipeline_info/software_versions.csv"
     *     },
     *     "pipelineReport": {
     *       "name": "pipeline_report.txt",
     *       "fileType": "txt",
     *       "path": "./pipeline_info/pipeline_report.txt"*
     *       },
     *     "executionReport": {
     *       "name": "execution_report.txt",
     *       "fileType": "txt",
     *       "path": "./pipeline_info/execution_report.txt"
     *     }
     *   }
     * @param pipelineInformation the folder containing the pipeline information
     * @return the folder containing the pipeline information with extracted properties as keys
     * @since 1.8.0
     */
    private static void parsePipelineInformation(Map pipelineInformation) {

        pipelineInformation.get("children").each { Map child ->
            switch (child.get("name")) {
                case "software_versions.csv":
                    insertAsProperty(pipelineInformation, child, RequiredPipelineFileKeys.SOFTWARE_VERSIONS.getKeyName())
                    break
                case "execution_report.txt":
                    insertAsProperty(pipelineInformation, child, RequiredPipelineFileKeys.EXECUTION_REPORT.getKeyName())
                    break
                case "pipeline_report.txt":
                    insertAsProperty(pipelineInformation, child, RequiredPipelineFileKeys.PIPELINE_REPORT.getKeyName())
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

    /**
     * Method which converts a map into json String
     * @param map a nested map representing a fileTree structure
     * @return json JSON String containing the mapped fileTree structure
     * @since 1.8.0
     */
    private static String mapToJson(Map map) {
        ObjectMapper jsonMapper = new ObjectMapper()
        String json = jsonMapper.writeValueAsString(map)
        return json
    }

    /**
     * Method which checks if a given Json String matches a given Json schema
     * @param json Json String which will be compared to schema
     * @param  path to Json schema for validation of Json String
     * @throws org.everit.json.schema.ValidationException
     */
    private static void validateJson(String json) throws ValidationException {
        // Step1: load schema
        JSONObject jsonObject = new JSONObject(json)
        InputStream schemaStream = PipelineOutput.getSchemaAsStream()
        JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream))
        SchemaLoader jsonSchemaLoader = SchemaLoader.builder()
                .schemaClient(SchemaClient.classPathAwareClient())
                .schemaJson(rawSchema)
                .resolutionScope("classpath://schemas/")
                .build()
        Schema jsonSchema = jsonSchemaLoader.load().build()

        // Step2: validate against schema return if valid, throw exception if invalid
        jsonSchema.validate(jsonObject)
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
                    throw new ParseException("Specified directory is empty", -1)
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
         * @since 1.8.0
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
         * @since 1.8.0
         */
        private static Map convertFile(Path path) {
            File currentFile = new File(path.toString())
            String name = currentFile.getName()
            String fileType = determineFileType(name)


            def convertedFile = [
                    "name"     : name,
                    "path"     : path,
                    "fileType": fileType
            ]
            return convertedFile
        }

        /**
         * This method extracts the file type also called extension from the filename.
         * The type defaults to the substring after the last `.` character in the string.
         * @param fileName the full name of the file including extension
         * @return the extension of the filename that was provided
         * @since 1.8.0
         */
        private static String determineFileType(String fileName) {
            // defaults to the string following the last '.' in the filename
            String fileType = fileName.tokenize('.').last()

            return fileType
        }

    }




}