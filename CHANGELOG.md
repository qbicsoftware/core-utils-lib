# Changelog for new releases

# 1.7.1
* Introduce secure connection to new nexus repository

# 1.7.0
* Use new parent pom 1.3.1 and integrate new nexus repository

# 1.6.0
* Use data-model-lib version 1.9.4, which introduces proper `unclassified folder` handling

# 1.5.0

* Ignore the quality control folder for now
* Acquire the Oxford Nanopore instrument output schema from the `data-model-lib`
* Support `unclassified` folders, that hold read information with missing and/or unknown sample code to the experiment

## 1.4.4

* Validation errors of the Nanopore file structure are now reported to the log more verbosely.

## 1.4.3

* Provides functionality to parse Nanopore data structure

## 1.4.2

* Use data model lib version 1.8.2, which introduces an important bug fix

## 1.4.1

* Provide JAR with all dependencies included for single deployment (i.e. ETL dropboxes, etc.)

## 1.4.0

* Add a new static class `NanoporeParser` that is able to parse a Oxford Nanopore Machine data output and provide its content as a core data model of type `OxfordNanoporeExperiment.class`.
