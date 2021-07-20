==========
Changelog
==========

This project adheres to `Semantic Versioning <https://semver.org/>`_.


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
