package life.qbic.services

import groovy.json.JsonOutput

import java.nio.file.FileAlreadyExistsException
import java.nio.file.NotDirectoryException

class NanoporeParser {

    private LinkedHashMap fileTreeMap
    private JsonOutput fileTreeJson


    NanoporeParser() {

    }

    /**
     * @return Linked HashMap of FileTree as Json File
     */
    Map parseAsMap(Path nanoporeDataDirectory) {

        String originDirPath = checkFilePath(filePath)
        File dirNameFile = new File(originDirPath)
        String origDirName = dirNameFile.getName()
        fileTreeMap = storeFileTreeInMap(originDirPath)
        fileTreeJson = createJson(fileTreeMap)
        saveJsonToFilePath(originDirPath, fileTreeJson, origDirName)
    }

    /**
     * @return filepath as a string if it consists of a directory
     */

    private String checkFilePath(String filePath) {

        try {

            if (filePath == null) {
                print("Please specify origin File Tree Directory ")
                filePath = System.in.newReader().readLine()
            }
            File userDirFile = new File(filePath)
            if (userDirFile.isDirectory()) {

                return filePath
            } else {

                throw new NotDirectoryException()
            }
        }
        catch (NotDirectoryException e) {
            print("Please provide a valid Directory Path")
        }
        catch (Exception e) {
            print("Unexpected Exception occured during directory read process")
        }
    }


    /**
     * @return FileTree from DirectoryPath as LinkedHashMap
     */

    private LinkedHashMap storeFileTreeInMap(String filePath) {

        try {

            def fileTreeDir = new File(filePath)
            def fileTreeMap = [:]
            if (fileTreeDir.directory) {
                fileTreeMap[fileTreeDir.name] = fileTreeDir.listFiles().collect { storeFileTreeInMap(it.path) }
                return fileTreeMap
            } else {
                return fileTreeDir.name
            }
        }
        catch (Exception e) {
            print("Unexpected Exception occured during HashMap generation")
        }

    }

    /**
     * @return Json from LinkedHashMap
     */

    private JsonOutput createJson(LinkedHashMap map, Object jsonSchema = null) {
        try {
            def fileTreeJson = JsonOutput.toJson(map)

            /* ToDo Json Validation should happen here and only validated Json file should be returned
               *   https://www.newtonsoft.com/json/help/html/JsonSchema.htm for documentation
               *   Possible schemas hosted on https://github.com/nanoporetech/ont_h5_validator/tree/master/h5_validator/schemas */
            if (jsonschema != null) {


            } else {
                print("Returning unvalidated Json File")
                return fileTreeJson
            }
        }
        catch (Exception e) {
            print("Unexpected Exception occured during Json file generation")
        }
    }

    /**
     * @save Json File to specified DirectoryPath
     */

    private void saveJsonToFilePath(String filePath, String jsonFile, String origDirName) {

        try {

            String fileName = origDirName + ".Json"
            File saveFile = new File(filePath, fileName)

            if (saveFile.exists()) {
                print("File already exists, Should it be Overwritten? Please Input Y or N: ")
                String overwriteCheck = System.in.newReader().readLine()
                if (overwriteCheck.toUpperCase() != "Y") {
                    throw new FileAlreadyExistsException(fileName)
                }
            }
            saveFile.write(jsonFile)

        }
        catch (FileAlreadyExistsException e) {
            print("File already exists, please provide a different file name ")
        }
        catch (Exception e) {
            print("Unexpected Exception occured during Json save process ")
        }
    }

    /* ToDo Remove after confirming that parser works correctly */

    static void main(String[] args) {


        NanoporeParser nanoporeParser = new NanoporeParser()
        nanoporeParser.runParser()
    }
}


