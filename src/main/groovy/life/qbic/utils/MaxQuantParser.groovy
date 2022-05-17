package life.qbic.utils

import com.fasterxml.jackson.databind.ObjectMapper
import life.qbic.datamodel.datasets.MaxQuantRunResult
import life.qbic.datamodel.maxquant.MaxQuantOutput
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
 * <h1>Parser storing the fileTree of a maxQuant run output directory into JSON format</h1>
 * <br>
 * <p>Converts a fileTree generated by a maxQuant run into a JSON String
 * @since 1.9.0
 *
 */
class MaxQuantParser implements DatasetParser<MaxQuantRunResult> {

    /**
     * Contains the associated keys of the required files in the root directory
     *
     * This enum contains the keys which are associated with the required files of the root directory in the provided fileTree
     * @since 1.9.0
     */
    enum RequiredRootFileKeys {
        RUN_PARAMETERS("runParameters"),
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
     * Contains the associated keys of the required files in the txt directory
     *
     * This enum contains the keys which are associated with the required files in the txt directory of the provided fileTree
     * @since 1.9.0
     */
    enum RequiredTxtFileKeys {
        ALL_PEPTIDES("allPeptides"),
        EVIDENCE("evidence"),
        EXPERIMENTAL_DESIGN_TEMPLATE("experimentalDesignTemplate"),
        PARAMETERS("parameters"),
        PEPTIDES("peptides"),
        PROTEIN_GROUPS("proteinGroups"),
        SUMMARY("summary")

        private String keyName

        RequiredTxtFileKeys(String keyName) {
            this.keyName = keyName
        }

        String getKeyName() {
            return this.keyName
        }

    }

    /** {@InheritDoc} */
    @Override
    MaxQuantRunResult parseFrom(Path root) throws DataParserException, DatasetValidationException {
        try {
            Map fileTreeMap = parseFileStructureToMap(root)
            adaptMapToDatasetStructure(fileTreeMap)
            String json = mapToJson(fileTreeMap)
            validateJson(json)
            MaxQuantRunResult maxQuantRunResult = MaxQuantRunResult.createFrom(fileTreeMap)
            return maxQuantRunResult
        } catch (ValidationException validationException) {
            throw new DatasetValidationException(validationException)
        } catch(Exception e) {
            throw new DataParserException(e.message, e.cause)
        }
    }

    /**
     * Generates a map representing the provided folder structure
     * @param directory path of directory whose fileTree should be converted into map
     * @since 1.9.0
     */
    private static Map parseFileStructureToMap(Path directory) {
        Map fileTreeMap = DirectoryConverter.fileTreeToMap(directory)
        return fileTreeMap
    }

    /**
     * Method which adapts the parsed map of the root directory in place to the expected file structure.
     * @see {<a href="https://github.com/qbicsoftware/data-model-lib/blob/master/src/test/resources/examples/resultset/maxquant/valid-resultset-example.json">valid datastructure example</a>}
     *
     * After parsing all directories and files of the provided filestructure are contained in the children property of the root directory map entry.
     * The underlying datastructure however expects a mapping of the expected folders and files to specific keys in the map outside of the children property such as
     * runParameters: { "name": "mqpar.xml",
     *                   "path": "./mqpar.xml",
     *                   "children": [] }
     * @param map a nested map representing the parsed fileTree structure
     * @since 1.9.0
     */
    private static void adaptMapToDatasetStructure(Map map) {
        List<Map> rootChildren = map.get("children") as List<Map>
        rootChildren.each { currentChild ->
            if (currentChild.containsKey("children")) {
                //folder
                parseTxtFolder(map)
            } else if (currentChild.containsKey("fileType")) {
                //file
                String name = currentChild.get("name")
                if(name.equals("mqpar.xml")) {
                    insertAsProperty(map, currentChild, RequiredRootFileKeys.RUN_PARAMETERS.getKeyName())
                } else if(name.endsWith("sample_ids.txt")) {
                    insertAsProperty(map, currentChild, RequiredRootFileKeys.SAMPLE_ID.getKeyName())
                }
            }
        }
    }

    /**
     * Method which adapts the parsed content of the txt directory in place to the expected file structure.
     * @see {<a href="https://github.com/qbicsoftware/data-model-lib/blob/master/src/test/resources/examples/resultset/maxquant/valid-resultset-example.json">valid datastructure example</a>}
     *
     * After parsing the files of the txt directory and a potential intermediate directory, which itself is contained in the root directory.
     * The underlying datastructure however expects a mapping of the expected files as a Map entry in the root directory.
     * @param maxQuantInformation a nested map representing the parsed fileTree structure
     * @since 1.9.0
     */
    private static void parseSubfolderInformation(Map maxQuantInformation) {
        List<Map> rootFolderInformation = maxQuantInformation.get("children") as List<Map>
        def combinedFolderInformation
        def txtFolderInformation
        def summaryFolderInformation
        rootFolderInformation.findAll { map ->
            if (map.get("name") == "combined") {
                combinedFolderInformation = map.get("children")
            }
            if (map.get("name") == "txt") {
                txtFolderInformation = map.get("children")  as List<Map>
            }
        }
        if (combinedFolderInformation) {
            combinedFolderInformation.findAll { map ->
                if (map.get("name") == "txt") {
                    txtFolderInformation = map.get("children")  as List<Map>
                }
            }
        }
        if (txtFolderInformation) {
        txtFolderInformation.each { Map child ->
                switch (child.get("name")) {
                    case "allPeptides.txt":
                        insertAsProperty(maxQuantInformation, child, RequiredTxtFileKeys.ALL_PEPTIDES.getKeyName())
                        break
                    case "evidence.txt":
                        insertAsProperty(maxQuantInformation, child, RequiredTxtFileKeys.EVIDENCE.getKeyName())
                        break
                    case "experimentalDesignTemplate.txt":
                        insertAsProperty(maxQuantInformation, child, RequiredTxtFileKeys.EXPERIMENTAL_DESIGN_TEMPLATE.getKeyName())
                        break
                    case "parameters.txt":
                        insertAsProperty(maxQuantInformation, child, RequiredTxtFileKeys.PARAMETERS.getKeyName())
                        break
                    case "peptides.txt":
                        insertAsProperty(maxQuantInformation, child, RequiredTxtFileKeys.PEPTIDES.getKeyName())
                        break
                    case "proteinGroups.txt":
                        insertAsProperty(maxQuantInformation, child, RequiredTxtFileKeys.PROTEIN_GROUPS.getKeyName())
                        break
                    default:
                        if(child.get("name") == "summary") summaryFolderInformation = child.get("children") as List<Map>
                        //ignoring other children
                        break
                    }
                }
        }
        if(summaryFolderInformation){
            summaryFolderInformation.each{ Map child ->
                if (child.get("name").toString().matches("summary_[0-9]{4}.*")) {
                    insertAsProperty(maxQuantInformation, child, RequiredTxtFileKeys.SUMMARY.getKeyName())
                }
            }
        }
    }

    /**
     * Inserts a map content into the provided parent map using the propertyName as key
     * @param parent the map to be modified
     * @param content the content to be added under the new key
     * @param propertyName the newly added key
     * @since 1.9.0
     */
    private static void insertAsProperty(Map parent, Object content, String propertyName) {
        parent.put(propertyName, content)
    }

    /**
     * Method which converts a map into json String
     * @param map a nested map representing a fileTree structure
     * @return json JSON String containing the mapped fileTree structure
     * @since 1.9.0
     */
    private static String mapToJson(Map map) {
        ObjectMapper jsonMapper = new ObjectMapper()
        String json = jsonMapper.writeValueAsString(map)
        return json
    }

    /**
     * Method which checks if a given Json String matches a given Json schema
     * @param json Json String which will be compared to schema
     * @throws org.everit.json.schema.ValidationException
     */
    private static void validateJson(String json) throws ValidationException {
        // Step1: load schema
        JSONObject jsonObject = new JSONObject(json)
        InputStream schemaStream = MaxQuantOutput.getSchemaAsStream()
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
         * @since 1.9.0
         * @throws FileNotFoundException in case the given directory does not exist
         * @throws IOException the input path could not be processed
         * @throws ParseException in case the directory is empty
         */
        static Map fileTreeToMap(Path path) throws FileNotFoundException, IOException, ParseException {
            File rootLocation = new File(path.toString())
            if (rootLocation.isFile()) {
                throw new NotDirectoryException("Expected a directory. Got a file instead.")
            } else if (rootLocation.isDirectory()) {
                //Check if existing Directory is empty
                if (rootLocation.list().length > 0) {
                    // Recursive conversion
                    Map folderStructure = convertDirectory(rootLocation.toPath())
                    return convertToRelativePaths(folderStructure, rootLocation.toPath())
                } else {
                    throw new ParseException("Specified directory ${path.toString()} is empty", -1)
                }
            } else {
                if (!rootLocation.exists()) {
                    throw new FileNotFoundException("The given path '${path.toString()}' does not exist.")
                } else {
                    throw new IOException("")
                }
            }

        }

        /**
         * Convert a directory structure to a map, following the maxQuant schema.
         * @param path a path to the current location in recursion
         * @return a map representing a directory with name, path and children as keys
         * @since 1.9.0
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
         * @param path a path to the current file in recursion
         * @return a map representing the file with name, path and file_type as keys
         * @since 1.9.0
         */
        private static Map convertFile(Path path) {
            File currentFile = new File(path.toString())
            String name = currentFile.getName()
            String fileType = determineFileType(name)


            def convertedFile = [
                    "name"    : name,
                    "path"    : path,
                    "fileType": fileType
            ]
            return convertedFile
        }

        /**
         * This method extracts the file type also called extension from the filename.
         * The type defaults to the substring after the last `.` character in the string.
         * @param fileName the full name of the file including extension
         * @return the extension of the filename that was provided
         * @since 1.9.0
         */
        private static String determineFileType(String fileName) {
            // defaults to the string following the last '.' in the filename
            String fileType = fileName.tokenize('.').last()
            return fileType
        }
    }
}
