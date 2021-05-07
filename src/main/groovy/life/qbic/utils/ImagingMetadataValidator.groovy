package life.qbic.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Log4j2
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import life.qbic.datamodel.datasets.imaging.ImageMetadata

@Log4j2
class ImagingMetadataValidator {

    /**
     * Validates metadata properties for one imaging dataset
     * @param propertyMap Map of key - value pairs of metadata for one dataset
     */
    static void validateImagingProperties(Map propertyMap) {
        // Step1: convert properties to json

        String json = mapToJson(propertyMap)
        try {
        // Step2: Validate created Json against schema 
            validateJson(json)
        } catch (ValidationException validationException) {
            log.error("Specified properties could not be validated")
            // we have to fetch all validation exceptions
            def causes = validationException.getAllMessages().collect{ it }.join("\n")
            log.error(causes)
            throw validationException
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
     * Method which checks if a given Json String matches the Imaging metadata Json schema
     * @param json Json String which will be compared to schema
     * @throws org.everit.json.schema.ValidationException
     */
    private static void validateJson(String json) throws ValidationException {
        // Step1: load schema
        JSONObject jsonObject = new JSONObject(json)
        InputStream schemaStream = ImageMetadata.getSchemaAsStream()
        JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream))
        Schema jsonSchema = SchemaLoader.load(rawSchema)
        // Step2: validate against schema return if valid, throw exception if invalid
        jsonSchema.validate(jsonObject)
    }
}
