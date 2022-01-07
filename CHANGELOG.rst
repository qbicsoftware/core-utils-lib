==========
Changelog
==========

This project adheres to `Semantic Versioning <https://semver.org/>`_.

1.10.4 (2022-01-04)
------------------

**Added**

**Fixed**

* CVE-2021-44832

**Dependencies**

* ``org.apache.logging.log4j:log4j-core:2.17.0`` -> ``2.17.1``
* ``org.apache.logging.log4j:log4j-api:2.17.0`` -> ``2.17.1``

**Deprecated**

1.10.3 (2021-12-20)
-------------------

**Added**

**Fixed**

* CVE-2021-45105

**Dependencies**

* org.apache.logging.log4j:log4j-api:2.16.0 -> 2.17.0
* org.apache.logging.log4j:log4j-core:2.16.0 -> 2.17.0

**Deprecated**

1.10.2 (2021-12-16)
-------------------

**Added**

**Fixed**

* CVE-2021-45046

**Dependencies**

* micronaut-http-client:io.micronaut:1.2.11 -> 2.5.13
* spock-core:org.spockframework:1.3-groovy-2.5 -> 2.0-groovy-2.5
* org.mockito:mockito-all:1.8.4 -> 1.10.19
* org.mockito:mockito-core:2.18.3 -> 4.1.0
* org.powermock:powermock-reflect:1.6.1 -> 2.0.9
* org.apache.commons:commons-lang3:3.7 -> 3.12.0
* org.apache.logging.log4j:log4j-api:2.15.0 -> 2.16.0
* org.apache.logging.log4j:log4j-core:2.15.0 -> 2.16.0

**Deprecated**


1.10.1 (2021-12-13)
-------------------

**Added**

**Fixed**

* CVE-2021-44228

**Dependencies**

* ``org.apache.logging.log4j:log4j-api:2.13.2`` -> ``2.15.0``

* ``org.apache.logging.log4j:log4j-core:2.13.2`` -> ``2.15.0``

**Deprecated**


1.10.0 (2021-09-13)
-------------------

**Added**

* Update 'life.qbic.utils.MaxQuantParser', parsing the summary file from a new summary folder (`#74 <https://github.com/qbicsoftware/core-utils-lib/pull/74>`_)

**Fixed**

**Dependencies**

**Deprecated**

1.9.3 (2021-07-20)
------------------

**Added**

**Fixed**

* Checks ``life.qbic.datasets.parsers.DatasetValidationException`` argument for null, throws an NPE immediately.

* Remove DatasetValidationException signature from ``adaptMapToDatasetStructure`` method in ``life.qbic.utils.MaxQuantParser.groovy``

**Dependencies**

**Deprecated**


1.9.2 (2021-07-20)
------------------

**Added**

**Fixed**

* Removes orphaned log statement

* Update 'life.qbic.utils.MaxQuantParser', parsing the summary file from a new summary folder (`#74 <https://github.com/qbicsoftware/core-utils-lib/pull/74>`_)

**Dependencies**

**Deprecated**

1.9.1 (2021-07-20)
------------------

**Added**

**Fixed**

* Removes unnecessary print and log statements in several parsers (`#65 <https://github.com/qbicsoftware/core-utils-lib/pull/65>`_)

**Dependencies**

**Deprecated**

1.9.0 (2021-07-19)
------------------

**Added**

* Introduce ``life.qbic.utils.MaxQuantParser`` to validate the filestructure resulting from a maxQuant run  (`#60 <https://github.com/qbicsoftware/core-utils-lib/pull/60>`_)

**Fixed**

* Ensures, that the BioinformaticAnalysisParser throws only exceptions as stated in the DataSetParser interface (`#62 <https://github.com/qbicsoftware/core-utils-lib/pull/62>`_)

**Dependencies**

**Deprecated**


1.8.0 (2021-05-07)
------------------

**Added**

* Add qube support (`#39 <https://github.com/qbicsoftware/core-utils-lib/pull/39>`_)

* Add DatasetParser as interface for parsing datasets ``life/qbic/datasets/parsers/DatasetParser.groovy`` (`#49 <https://github.com/qbicsoftware/core-utils-lib/pull/49>`_)

* Add ImagingMetadataValidator for validating json files containing imaging metadata ``life/qbic/utils/ImagingMetadataValidator.groovy``, with test ``life/qbic/utils/ImagingMetadataValidatorSpec.groovy`` (`#48 <https://github.com/qbicsoftware/core-utils-lib/pull/48>`_)

* Add BioinformaticAnalaysisParser to validate the filestructure resulting from Nfcore pipeline output ``life.qbic.utils.BioinformaticAnalysisParser`` (`#51 <https://github.com/qbicsoftware/core-utils-lib/pull/51>`_)

**Fixed**

**Dependencies**

* Upgrade ``life.qbic:groovy:data-model-lib:jar:2.4.0`` -> ``2.7.0``

* Downgrade java 11 -> java 8

* Remove parent-pom and introduce a slim own definition of dependencies.

* Re-introduce ``org.apache.commons.commons-lang3`` dependency with version ``3.7.0``

**Deprecated**


1.7.2 (2021-05-07)
------------------

**Added**

* Use data-model-lib version 2.4.0

* Introduce secure connection to new nexus repository

**Fixed**

**Dependencies**

**Deprecated**


1.7.0 (2021-05-07)
------------------

**Added**

* Introduced new generic interface ``life.qbic.datasets.parsers.DatasetParser`` that can be used to
implement dataset parsers for different dataset types.

**Fixed**

**Dependencies**

**Deprecated**


0.1.0 (2020-12-02)
------------------

**Added**

* Created the project using cookietemple

**Fixed**

**Dependencies**

**Deprecated**
