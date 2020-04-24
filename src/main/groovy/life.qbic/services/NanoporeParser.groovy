import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput

import java.nio.file.Path
import java.nio.file.Paths

class Test {

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

    private JsonOutput createJson(LinkedHashMap map, Object jsonSchema = null) {
        try {
            def fileTreeJson = JsonOutput.toJson(map)
            /* ToDo Json Validation should happen here and only validated Json file should be returned
               *   https://www.newtonsoft.com/json/help/html/JsonSchema.htm for documentation
               *   Possible schemas hosted on https://github.com/nanoporetech/ont_h5_validator/tree/master/h5_validator/schemas */
            if (jsonSchema != null) {


            } else {
                print("Returning unvalidated Json File")
                return fileTreeJson
            }
        }
        catch (Exception e) {
            print("Unexpected Exception occured during Json file generation")
        }
    }

/* ToDo remove!, For Testing purposes only  */

    static void main(String[] args) {
        Test test = new Test()
        Path testPath = Paths.get("/Users/steffengreiner/Desktop/testdir/")
        test.parseAsMap(testPath)
        print(test.fileMap)
        //def json = test.createJson(test.fileMap)
        //print(json)

    }

}
