package life.qbic.utils

import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import javax.validation.ValidationException
import java.nio.file.Path
import java.nio.file.Paths
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener
import java.util.stream.Stream;

class NanoporeParser {

    Path targetDirectoryPath
    Path schemaPath

    private NanoporeParser() {
        throw new AssertionError()
    }
    NanoporeParser(Path targetDirectoryPath, Path schemaPath) {
        this.targetDirectoryPath = targetDirectoryPath
        this.schemaPath = schemaPath

    }
    Map<Object, Object> fileMap = new HashMap<>()

    class NanoporeDirectory {
        String name
        Path path
        ArrayList children


        private NanoporeDirectory() {
            throw new AssertionError()
        }

        NanoporeDirectory(String name, Path path, List children) {
            this.name = name
            this.path = path
            this.children = children
        }
    }

    class NanoporeFile {
        String name
        Path path
        String extension

        private NanoporeFile() {
            throw new AssertionError()
        }

        NanoporeFile(String name, Path path, String extension) {
            this.name = name
            this.path = path
            this.extension = extension
        }
    }

    private void directoryToMap(Path filePath) {

        File currentFile = new File(filePath.toString())
        String currentFullFileName = currentFile.getName()
        Path currentPathName = filePath

        /* ToDo Find solution to map directories correctly, currently all directories are put into singular map */
        if (currentFile.isDirectory()) {

            String dirName = currentFullFileName
            Path dirPath = currentPathName
            List children = []
            NanoporeDirectory nanoporeDirectory = new NanoporeDirectory(dirName, dirPath, children)

            currentFile.eachFileRecurse { file ->
                nanoporeDirectory.children.add(file)
                directoryToMap(file.toPath())
            }

            this.fileMap.put(nanoporeDirectory.getName(), nanoporeDirectory)

        } else {

            String[] splitFileName = fileNameSplit(currentFile)
            String fileName = splitFileName[0]
            String fileExtension = splitFileName[1]

            NanoporeFile nanoporeFile = new NanoporeFile(fileName, currentPathName, fileExtension)
            fileMap.put(nanoporeFile.getName(), nanoporeFile)
        }

    }

    private String[] fileNameSplit(File file) {
        String fullFileName = file.getName()
        //ToDo What about Zip Files?
        String[] splitFileName = fullFileName.split('\\.(?=[^\\.]+$)')

        String fileName
        String fileExtension

        if (splitFileName.size() > 1)
            fileExtension = splitFileName[1]
        else {
            fileExtension = "None"
        }
        if (splitFileName[0].size() < 1) {
            fileName = splitFileName[1]
            fileExtension = "None"
        } else {
            fileName = splitFileName[0]
        }

        String[] resultingFileArray = [fileName, fileExtension]
        return resultingFileArray
    }

    private String createJson(Map map) {

        try {
            // Mapping according to https://howtodoinjava.com/jackson/jackson-json-to-from-hashmap/
            ObjectMapper jsonMapper = new ObjectMapper()
            String jsonFromMap = jsonMapper.writeValueAsString(map)
            return jsonFromMap
        }
        catch (JsonGenerationException e) {
            print("Json could not be generated")
        } catch (JsonMappingException e) {
            print("Internal Map could not be converted to Json")
        }
    }
    //Validation according to documentation found on https://github.com/everit-org/json-schema
    private void validateJson(String json, Schema jsonSchema) {

        try {
            jsonSchema.validate(json)
        }
        catch (ValidationException e) {
            print("Structure of Input Json File did not match specified Jsonschema")
            e.printStackTrace()
        }
        catch (Exception e) {
            print("Unexpected Exception occurred")
            e.printStackTrace()
        }
    }
    //Json schema generation according to documentation found on https://github.com/everit-org/json-schema
    private Schema fileToSchema(Path path) {
        //ToDo InputStream returns null no matter what path is specified
        InputStream jsonSchemaStream = getClass().getResourceAsStream(path.toString())
        JSONObject jsonSchemaObject = new JSONObject(new JSONTokener(jsonSchemaStream as Stream))
        Schema jsonSchema = SchemaLoader.load(jsonSchemaObject)
        return jsonSchema
    }

/* ToDo remove!, For Testing purposes only  */

    static void main(String[] args) {
        Path dirPath = Paths.get("/Users/steffengreiner/Desktop/testdir/")
        Path schemaPath = Paths.get("src/main/resources/nanopore-instrument-output.schema.json")
        NanoporeParser nanoporeParser = new NanoporeParser(dirPath, schemaPath)

        nanoporeParser.directoryToMap(dirPath)
        String json = nanoporeParser.createJson(nanoporeParser.fileMap)

        Schema jsonSchema = nanoporeParser.fileToSchema(schemaPath)
        nanoporeParser.validateJson(json, jsonSchema)
    }

}
