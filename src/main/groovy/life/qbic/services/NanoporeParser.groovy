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

    Map<Object, Object> fileMap = new HashMap<>()

    class NanoporeDirectory {
        ArrayList children
        Path dirPath
        String dirName
    }

    class NanoporeFile {
        Path filePath
        String fileName
        String fileExtension
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
            nanoporeDirectory.children = []
            currentFile.eachFileRecurse { file ->
                nanoporeDirectory.children.add(file)
                parseAsMap(Paths.get(file.path))
            }
            fileMap.put(nanoporeDirectory.dirName, nanoporeDirectory)

        } else {
            NanoporeFile nanoporeFile = new NanoporeFile()
            nanoporeFile.fileName = currentFileName
            nanoporeFile.filePath = currentPathName
            // Get File Extension by searching for last occurrence and account for files with missing file extension
            int i = currentFileName.lastIndexOf('.')
            String currentFileExtension = i > 0 ? currentFileName.substring(i + 1) : ""
            nanoporeFile.fileExtension = currentFileExtension
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
            print("Json could not be generated")
        } catch (JsonMappingException e) {
            print("Internal Map could not be converted to Json")
        }
    }

    private void validateJson(String json, InputStream jsonSchemaStream) {

        JSONObject jsonSchemaObject = new JSONObject(new JSONTokener(jsonSchemaStream as Stream))
        //Validation according to documentation found on https://github.com/everit-org/json-schema
        try {

            Schema jsonSchema = SchemaLoader.load(jsonSchemaObject)
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

/* ToDo remove!, For Testing purposes only  */

    static void main(String[] args) {
        NanoporeParser nanoporeParser = new NanoporeParser()
        Path testPath = Paths.get("/Users/steffengreiner/Desktop/testdir/")
        nanoporeParser.parseAsMap(testPath)
        print(nanoporeParser.fileMap)
        String json = nanoporeParser.createJson(nanoporeParser.fileMap)
        print(json)
        //ToDo Inputstream returns null no matter what path is specified
        InputStream jsonSchemaStream = getClass().getResourceAsStream("/Users/steffengreiner/Documents/GitHub/Work/core-utils-lib/src/main/resources/nanopore-instrument-output.schema.json")
        nanoporeParser.validateJson(json, jsonSchemaStream)

    }

}
