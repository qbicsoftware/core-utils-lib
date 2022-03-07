package life.qbic.utils

import org.everit.json.schema.ValidationException
import spock.lang.Specification

class ImagingMetadataValidatorSpec extends Specification {

  def "a valid object does not throw an error"() {
    given:
    Map propertyMap = new HashMap<>()
    propertyMap.put("image_filename","myimage.tiff")
    propertyMap.put("imaging_modality","CT")
    propertyMap.put("imaging_date","21.12.2011")
    propertyMap.put("instrument_user","Arnold Schwarzenegger")
    propertyMap.put("instrument_manufacturer","FN")
    propertyMap.put("imaged_tissue","brain")
    when:
    ImagingMetadataValidator.validateImagingProperties(propertyMap)
    then:
    noExceptionThrown()
  }

  def "validating an object with correct properties but incorrect values throws a ValidationError"() {
    given:
    Map propertyMap = new HashMap<>()
    propertyMap.put("image_filename","myimage.tiff")
    propertyMap.put("imaging_modality","CT")
    propertyMap.put("imaging_date","21.12.2011")
    propertyMap.put("instrument_user",6)
    propertyMap.put("instrument_manufacturer","FN")
    propertyMap.put("imaged_tissue","brain")
    when:
    ImagingMetadataValidator.validateImagingProperties(propertyMap)
    then:
    thrown(ValidationException)
  }

  def "validating an object with correct properties but incorrect imaging_date throws a ValidationError"() {
    given:
    Map propertyMap = new HashMap<>()
    propertyMap.put("image_filename","myimage.tiff")
    propertyMap.put("imaging_modality","CT")
    propertyMap.put("imaging_date","30122011")
    propertyMap.put("instrument_user","Arnold Schwarzenegger")
    propertyMap.put("instrument_manufacturer","FN")
    propertyMap.put("imaged_tissue","brain")
    when:
    ImagingMetadataValidator.validateImagingProperties(propertyMap)
    then:
    thrown(ValidationException)
  }

  def "validating an object with missing properties throws a ValidationError"() {
    given:
    Map propertyMap = new HashMap<>()
    propertyMap.put("image_filename","myimage.tiff")
    propertyMap.put("imaging_modality","CT")
    propertyMap.put("imaging_date","22.12.2011")
    propertyMap.put("instrument_user","Arnold Schwarzenegger")
    propertyMap.put("instrument_manufacturer","FN")
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
