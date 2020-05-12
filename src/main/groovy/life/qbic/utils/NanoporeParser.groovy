package life.qbic.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

import java.nio.file.NotDirectoryException
import java.nio.file.Path


@Log4j2
class NanoporeParser {

    final static private JSON_SCHEMA = "/nanopore-instrument-output.schema.json"
    final private Path targetDirectoryPath

    private NanoporeParser() {
        throw new AssertionError()
    }

    NanoporeParser(Path targetDirectoryPath) {
        this.targetDirectoryPath = targetDirectoryPath
    }

    /**
     * Method where all the magic of the nanopore parser takes place
     * @param directory path of directory whose fileTree should be converted into map
     */
    static Map parseFileStructure(Path directory) {
        // Step1: convert directory to json
        Map convertedDirectory = DirectoryConverter.fileTreeToMap(directory)
        // Step2: validate json
        String json = mapToJson(convertedDirectory)
        if (isValidJsonForSchema(json, JSON_SCHEMA))
        //Step3: return valid json as Map
        {
            parseMetaData(convertedDirectory)
            return convertedDirectory
        }


        //ToDo Add Possibility of returning Json as String?
    }


    private static Map parseMetaData(Map<String, Map> convertedDirectory) {
        convertedDirectory.get("children").each { measurement ->
            def reportFile = measurement["children"].find {it["name"].contains("report") && it["file_type"] == "md"}
            def summaryFile = measurement["children"].find {it["name"].contains("final_summary") && it["file_type"] == "txt"}
            def metadata = readMetaData(reportFile as Map, summaryFile as Map)
            measurement["metadata"] = metadata
        }
        return convertedDirectory
    }

    private static Map readMetaData(Map<String, String> reportFile, Map<String, String> summaryFile) {
        def report = new File(reportFile["path"]).readLines().iterator()
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
                buffer.append(line)
            }
            if (line.startsWith("}")) {
                jsonEnded = true
            }
        }
        def finalMetaData = (Map) jsonSlurper.parseText(buffer.toString())

        new File(summaryFile["path"]).readLines().each { line ->
            def split = line.split("=")
            finalMetaData[split[0]] = split[1]
        }

        return finalMetaData
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
     * @param schema path to Json schema for validation of Json String
     */
    private static boolean isValidJsonForSchema(String json, String schema) {
        // Step1: load schema
        try {
            JSONObject jsonObject = new JSONObject(json)
            InputStream schemaStream = NanoporeParser.getResourceAsStream(schema)
            JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream))
            Schema jsonSchema = SchemaLoader.load(rawSchema)
            // Step2: validate against schema return true if valid, throw exception if invalid
            jsonSchema.validate(jsonObject)
            return true
        }
        catch (JSONException e) {
            log.error("JsonObject could not be generated")
            e.printStackTrace()

        }
        catch (ValidationException e) {
            log.error("Json did not match Json Schema")
            print(e.getAllMessages())
            return false
        }
    }
    /**
     * Converts a file tree into a json object.
     */
    private static class DirectoryConverter {
        private static final PREDEFINED_EXTENSIONS = ["fastq.gz"]

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

        /**
         *
         * @param a path to the current file in recursion
         * @return a map representing the file with name, path and file_type as keys
         */
        private static Map convertFile(Path path) {
            // convert to File object
            File currentFile = new File(path.toString())
            String name = currentFile.getName()
            // defaults to the string following the last '.' in the filename
            String fileType = name.tokenize('.').last()
            // check for predefined file type extensions
            for (extension in PREDEFINED_EXTENSIONS) {
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
