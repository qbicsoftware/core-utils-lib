package life.qbic.utils

import groovyjarjarcommonscli.MissingArgumentException
import life.qbic.datamodel.datasets.imaging.ImageMetadata
import org.everit.json.schema.ValidationException
import spock.lang.Specification

class ImagingMetadataValidatorSpec extends Specification {

  def "a valid object does not throw an error"() {
    given:
    Map propertyMap = new HashMap<>()
    propertyMap.put()
    propertyMap.put()
    propertyMap.put()
    propertyMap.put()
    propertyMap.put()
    when:
    ImagingMetadataValidator.validateImagingProperties(propertyMap)
    then:
    noExceptionThrown()
  }

  def "validating an object with correct properties but incorrect values throws a ValidationError"() {
    given:
    Map propertyMap = new HashMap<>()
    propertyMap.put()
    propertyMap.put()
    propertyMap.put()
    propertyMap.put()
    propertyMap.put()
    when:
    ImagingMetadataValidator.validateImagingProperties(propertyMap)
    then:
    thrown(ValidationException)
  }

  def "validating an object with missing properties throws a ValidationError"() {
    given:
    Map propertyMap = new HashMap<>()
    propertyMap.put()
    propertyMap.put()
    propertyMap.put()
    propertyMap.put()
    propertyMap.put()
    when:
    ImagingMetadataValidator.validateImagingProperties(propertyMap)
    then:
    thrown(ValidationException)
  }

  def "parsing and validating an empty map throws a ValidationError"() {
    given:
    Map propertyMap = new HashMap<>()
    when:
    ImagingMetadataValidator.validateImagingProperties(propertyMap)
    then:
    thrown(ValidationException)
  }
}