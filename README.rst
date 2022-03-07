Core Utilities Library
-----------------------------------

A collection of non-Vaadin, non-Liferay utilities.

|maven-build| |maven-test| |codeql| |release|
|license| |java| |groovy|

How to Run
-----------------

To build this library use Maven and Java 8:

First compile the project and build an executable java archive:

.. code-block:: bash

    mvn clean package

Note that you will need java 8.
The JAR file will be created in the /target folder:

.. code-block:: bash

    |-target
    |---core-utils-lib-1.0.0-SNAPSHOT.jar
    |---...


How to Use
----------

This is a library and the most common way to use this library in particular is by including it in your `pom.xml` as a dependency:

.. code-block:: xml

    <dependency>
      <groupId>life.qbic</groupId>
      <artifactId>core-utils-lib</artifactId>
      <version>X.Y.Z</version>
    </dependency>

Find QBiC services
------------------
Finding QBiC service instances from within an application is as easy as this:

.. code-block:: Groovy

    // Example in Groovy
    def serviceList = []
    def serviceRegistryUrl = new Url("https://host-name-of-registry:<port>/v1")
    def connector = new ConsulConnector(serviceRegistryUrl)
    connector.withCloseable {
        ConsulServiceFactory factory = new ConsulServiceFactory(it)
        serviceList.addAll(factory.getServicesOfType(ServiceType.SAMPLE_TRACKING))
    }


.. code-block:: Java

    // Example in Java
    List serviceList = new ArrayList<>()
    Url serviceRegistryUrl = new URL("https://host-name-of-registry:<port>/v1")
    try (ConsulConnector connector = new ConsulConnector(serviceRegistryUrl)) {
        ConsulServiceFactory factory = new ConsulServiceFactory(connector)
        serviceList.addAll(factory.getServicesOfType(ServiceType.SAMPLE_TRACKING))
    }


License
-------

This work is licensed under the `MIT license <https://mit-license.org/>`_.

**Note**: This work uses the `Micronaut Framework <https://github.com/micronaut-projects>`_ and derivatives from the Micronaut framework family, its which are licensed under `Apache 2.0 <https://www.apache.org/licenses/LICENSE-2.0>`_.


.. |maven-build| image:: https://github.com/qbicsoftware/core-utils-lib/workflows/Build%20Maven%20Package/badge.svg
    :target: https://github.com/qbicsoftware/core-utils-lib/actions/workflows/build_package.yml
    :alt: Github Workflow Build Maven Package Status

.. |maven-test| image:: https://github.com/qbicsoftware/core-utils-lib/workflows/Run%20Maven%20Tests/badge.svg
    :target: https://github.com/qbicsoftware/core-utils-lib/actions/workflows/run_tests.yml
    :alt: Github Workflow Tests Status

.. |codeql| image:: https://github.com/qbicsoftware/core-utils-lib/workflows/CodeQL/badge.svg
    :target: https://github.com/qbicsoftware/core-utils-lib/actions/workflows/codeql-analysis.yml
    :alt: CodeQl Status

.. |license| image:: https://img.shields.io/github/license/qbicsoftware/core-utils-lib
    :target: https://github.com/qbicsoftware/core-utils-lib/blob/master/LICENSE
    :alt: Project Licence

.. |release| image:: https://img.shields.io/github/v/release/qbicsoftware/core-utils-lib.svg?include_prereleases
    :target: https://github.com/qbicsoftware/core-utils-lib/releases
    :alt: Release status

.. |java| image:: https://img.shields.io/badge/language-java-blue.svg
    :alt: Written in Java

.. |groovy| image:: https://img.shields.io/badge/language-groovy-blue.svg
    :alt: Written in Groovy