package life.qbic.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import life.qbic.datamodel.instruments.OxfordNanoporeInstrumentOutputDoradoMinimal
import life.qbic.datamodel.instruments.OxfordNanoporeInstrumentOutputMinimal
import net.jimblackler.jsonschemafriend.Schema
import net.jimblackler.jsonschemafriend.SchemaStore
import net.jimblackler.jsonschemafriend.ValidationException
import net.jimblackler.jsonschemafriend.Validator

import java.nio.file.NotDirectoryException
import java.nio.file.Path
import java.nio.file.Paths
import java.text.ParseException
import life.qbic.datamodel.datasets.OxfordNanoporeExperiment
import java.util.stream.Collectors

@Log4j2
class NanoporeParser {

    private static Set<File> hiddenFiles = new HashSet<>()
    /**
     * Generates a map representing the folder structure, if it is a correct structure
     * Deletes any hidden files, if the structure fits one of the Nanopore models
     * @param directory path of directory whose fileTree should be converted into map
     */
    static OxfordNanoporeExperiment parseFileStructure(Path directory) {
        // Step1: convert directory to json
        Map convertedDirectory = DirectoryConverter.fileTreeToMap(directory)

        String json = mapToJson(convertedDirectory)
        // Step2: Validate created Json against schema
        validateJson(json)
        //Step3: convert valid json to OxfordNanoporeExperiment Object
        // Step4: Parse meta data out of report files and extend the map
        def finalMap = parseMetaData(convertedDirectory, directory)
        // Step5: Create the final OxfordNanoporeExperiment from the map
        OxfordNanoporeExperiment convertedExperiment = OxfordNanoporeExperiment.create(finalMap)
        // Step6: This is a valid experiment, we can now delete the hidden files
        for (File hiddenFile : hiddenFiles) {
            deleteFile(hiddenFile)
        }

        return convertedExperiment

    }

    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteFile(child)
            }
        }
        file.delete()
    }

    /**
     * The main metadata we need to provide for the OxfordNanoporeExperiment is in
     * the report markdown file and final summary file.
     * The parsed metadata properties are summarized as key value pairs under an new map
     * key "metadata".
     */
    private static Map parseMetaData(Map convertedDirectory, Path root) {
        convertedDirectory.get("children").each { measurement ->
            def reportFile = measurement["children"].find { it["name"].contains("report") && it["file_type"] == "md" }
            def summaryFile = measurement["children"].find { it["name"].contains("final_summary") && it["file_type"] == "txt" }
            def metadata = readMetaData(reportFile as Map, summaryFile as Map, root)
            Map finalMetadata = finalizeMetadata(metadata)
            measurement["metadata"] = finalMetadata
        }
        return convertedDirectory
    }

    /**
     * The metadata contained in the report markdown is notated as an embedded JSON object in the header of the file.
     * The additional metadata contained in the final summary is a line-separated list of
     * key=value pairs.
     */
    private static Map readMetaData(Map<String, String> reportFile, Map<String, String> summaryFile, Path root) {
        def report = new File(Paths.get(root.toString(), reportFile["path"].toString()) as String)
                .readLines()
                .iterator()
        def buffer = new StringBuffer()
        def jsonSlurper = new JsonSlurper()
        def jsonStarted = false
        def jsonEnded = false
        while (report.hasNext()) {
            if (jsonEnded) {
                break
            }
            def line = report.next()
            if (line.startsWith("{")) {
                jsonStarted = true
            }
            if (jsonStarted) {
                def split = line.replaceAll("\\s+", "").split(":")
                if (split.size() == 2 && split[1].replaceAll('"', "").size() <= 1) {
                    log.info("Metadata value ${split[0]} missing in ${reportFile["path"]}")
                }
                buffer.append(line)
            }
            if (line.startsWith("}")) {
                jsonEnded = true
            }
        }

        def finalMetaData = (Map) jsonSlurper.parseText(buffer.toString())
        new File(Paths.get(root.toString(), summaryFile["path"].toString()) as String)
                .readLines().each { line ->
            def split = line.split("=")
            if (split.size() > 1) {
                finalMetaData[split[0]] = split[1]
            } else {
                log.info("Metadata value ${split[0]} missing in ${summaryFile["path"]}, defaulting to empty value")
                finalMetaData[split[0]] = ""
            }
        }
        return finalMetaData
    }

    /**
     * The base caller and flow cell position entries are not nicely stored in the metadata.
     * We refactor them to the properties, the data model OxfordNanoporeExperiment expects.
     */
    private static Map finalizeMetadata(Map metadataMap) {
        // Step1: Set entries that need to be changed in Metadata Map
        // Current base-caller information is stored in a Key-Value pair like "guppy_version": "3.2.8+bd67289"
        String basecallerName = "guppy_version"
        // Current flow cell position information is stored in a Key-Value pair like "position": "1-A3-D3"
        String flowcellPositionName = "position"
        // Step2: Get key and values for specified entries
        checkPresenceOfBaseCaller(metadataMap, basecallerName)
        checkPresenceOfFlowCellPosition(metadataMap, flowcellPositionName)

        metadataMap["base_caller"] = "guppy"
        metadataMap["base_caller_version"] = metadataMap[basecallerName]
        metadataMap["flow_cell_position"] = metadataMap[flowcellPositionName]

        return metadataMap
    }

    private static void checkPresenceOfFlowCellPosition(Map metadata, String flowCellEntry) {
        if (!metadata.containsKey(flowCellEntry)) {
            throw new RuntimeException("Could not find metadata information about the flow cell position.")
        }
        if ((metadata[flowCellEntry] as String).isEmpty()) {
            throw new RuntimeException("Flow cell position information was empty.")
        }
    }

    private static void checkPresenceOfBaseCaller(Map metadata, String baseCallerEntry) {
        if (!metadata.containsKey(baseCallerEntry)) {
            throw new RuntimeException("Could not find metadata information about the base caller.")
        }
        if ((metadata[baseCallerEntry] as String).isEmpty()) {
            throw new RuntimeException("Base caller information was empty.")
        }
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

    /**
     * Method which checks if a given Json String matches a given Json schema
     * @param json Json String which will be compared to schema
     * @param path to Json schema for validation of Json String
     * @throws net.jimblackler.jsonschemafriend.ValidationException
     */
    private static void validateJson(String json) throws ValidationException {
        // Step 1: load json
        ObjectMapper objectMapper = new ObjectMapper()
        Object jsonObject = objectMapper.readValue(json, Object)

        SchemaStore schemaStore = new SchemaStore()
        Validator validator = new Validator()
        try {
            //Validate against Fast5 Based Oxford Measurement
            Schema schema = schemaStore.loadSchema(OxfordNanoporeInstrumentOutputMinimal.getSchemaAsStream())
            validator.validate(schema, jsonObject)
        } catch (ValidationException ignored) {
            //Validate against Pod5 Based Oxford Measurement
            Schema schema = schemaStore.loadSchema(OxfordNanoporeInstrumentOutputDoradoMinimal.getSchemaAsStream())
            validator.validate(schema, jsonObject)
        }
    }

    /*
     * Converts a file tree into a json object.
     */

    private static class DirectoryConverter {
        private static final PREDEFINED_EXTENSIONS = ["fastq.gz"]
        private static final IGNORED_FOLDERNAMES = ["qc"]

        /**
         *
         * @param path a path to the directory which will be used as root for parsing
         * @return a Map describing the file tree starting from the given path
         */
        static Map fileTreeToMap(Path path) {
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
                    throw new ParseException("Parsed directory might not be empty", -1)
                }
            } else {
                if (!rootLocation.exists()) {
                    throw new FileNotFoundException("The given path does not exist.")
                } else {
                    throw new IOException()
                }
            }

        }

        /**
         * Convert a directory structure to a map, following the Nanopore schema.
         * Ignores hidden files in the structure and adds them to a global set to be
         * dealt with later.
         * @param a path to the current location in recursion
         * @return a map representing a directory with name, path and children as keys
         */
        private static Map convertDirectory(Path path) {
            // convert to File object
            File currentDirectory = new File(path.toString())
            String name = currentDirectory.getName()
            if (IGNORED_FOLDERNAMES.contains(name)) {
                return null
            }
            List<File> children = currentDirectory.listFiles()

            List<File> visibleChildren = children.stream()
                    .filter(file -> !file.isHidden()).collect(Collectors.toList())

            for (File file : children) {
                if (!visibleChildren.contains(file)) {
                    hiddenFiles.add(file)
                }
            }

            visibleChildren = visibleChildren.findAll { file ->
                String currentFolderName = file.getName()
                return !IGNORED_FOLDERNAMES.contains(currentFolderName)
            }.collect {
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
                    "children": visibleChildren
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
         * If the filename ends with one of the predefined extensions in
         * DirectoryConverter.PREDEFINED_EXTENSIONS then this extension is returned as fileType.
         * @param fileName the full name of the file including extension
         * @return the extension of the filename that was provided
         */
        private static String determineFileType(String fileName) {
            // defaults to the string following the last '.' in the filename
            String fileType = fileName.tokenize('.').last()
            // check for predefined file type extensions
            for (extension in PREDEFINED_EXTENSIONS) {
                if (fileName.endsWith(extension)) {
                    fileType = extension
                }
            }
            return fileType
        }

    }
}
