import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

import javax.validation.ValidationException
import java.nio.file.Path
import java.nio.file.Paths

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

class NanoporeParser {

    Map<Object, Object> fileMap = new HashMap<>()

    class NanoporeDirectory {
        Object children
        Path dirPath
        String dirName
    }

    class NanoporeFile {
        Path filePath
        String fileName
    }

    private void parseAsMap(Path filePath) {

        File currentFile = new File(filePath.toString())
        String currentFileName = currentFile.getName()
        Path currentPathName = filePath

        /* ToDo Find solution to map directories correctly, currently all directories are put into singular map */
        if (currentFile.directory) {

            NanoporeDirectory nanoporeDirectory = new NanoporeDirectory()
            nanoporeDirectory.dirName = currentFileName
            nanoporeDirectory.dirPath = currentPathName
            nanoporeDirectory.children = currentFile.eachFileRecurse { file ->
                parseAsMap(Paths.get(file.path))
            }

            fileMap.put(nanoporeDirectory.dirName, nanoporeDirectory)
        } else {
            NanoporeFile nanoporeFile = new NanoporeFile()
            nanoporeFile.fileName = currentFileName
            nanoporeFile.filePath = currentPathName
            fileMap.put(nanoporeFile.fileName, nanoporeFile)
        }

    }

    private String createJson(Map map) {

        try {
            // Mapping according to https://howtodoinjava.com/jackson/jackson-json-to-from-hashmap/
            ObjectMapper jsonMapper = new ObjectMapper()
            String jsonFromMap = jsonMapper.writeValueAsString(map)
            return jsonFromMap
        }
        catch (JsonGenerationException e) {
            print("Json could not be generated!")
        } catch (JsonMappingException e) {
            print("Internal Map could not be converted to Json")
        }
    }

    private void validateJson(String json, InputStream jsonSchemaStream) {

        //Validation according to documentation found on https://github.com/everit-org/json-schema
        try {
            JSONObject jsonSchemaObject = new JSONObject(new JSONTokener(jsonSchemaStream))
            Schema jsonSchema = SchemaLoader.load(jsonSchemaObject)
            jsonSchema.validate(json)
        }
        catch (ValidationException e) {
            print("Structure of Input Json File did not match specified Jsonschema")
        }
        catch (Exception e) {
            print("Unexpected Exception occurred")
        }
    }

/* ToDo remove!, For Testing purposes only  */

    static void main(String[] args) {
        NanoporeParser nanoporeParser= new NanoporeParser()
        Path testPath = Paths.get("/Users/steffengreiner/Desktop/testdir/")
        nanoporeParser.parseAsMap(testPath)
        print(nanoporeParser.fileMap)
        String json = nanoporeParser.createJson(nanoporeParser.fileMap)
        InputStream jsonSchemaStream = getClass().getResourceAsStream("/Users/steffengreiner/Documents/GitHub/Work/core-utils-lib/src/main/resources/nanopore-instrument-output.schema.json")
        nanoporeParser.validateJson(json, jsonSchemaStream)
        print(json)
    }

}
