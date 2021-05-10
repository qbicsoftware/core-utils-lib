==========
Changelog
==========

This project adheres to `Semantic Versioning <https://semver.org/>`_.


1.8.0 (2021-05-07)
------------------

**Added**

* Add qube support

* Add DatasetParser as interface for parsing datasets ``life/qbic/datasets/parsers/DatasetParser.groovy``

* Add ImagingMetadataValidator for validating json files containing imaging metadata ``life/qbic/utils/ImagingMetadataValidator.groovy``, with test ``life/qbic/utils/ImagingMetadataValidatorSpec.groovy``


**Fixed**

**Dependencies**

* Upgrade ``life.qbic:groovy:data-model-lib:jar:2.4.0`` -> ``2.6.0-SNAPSHOT``

* Downgrade java 11 -> java 8

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
