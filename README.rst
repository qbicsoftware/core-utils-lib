core-utils-lib
-----------------------------------

.. image:: https://github.com/qbicsoftware/core-utils-lib/workflows/Build%20Maven%20Package/badge.svg
    :target: https://github.com/qbicsoftware/core-utils-lib/workflows/Build%20Maven%20Package/badge.svg
    :alt: Github Workflow Build Maven Package Status

.. image:: https://github.com/qbicsoftware/core-utils-lib/workflows/Run%20Maven%20Tests/badge.svg
    :target: https://github.com/qbicsoftware/core-utils-lib/workflows/Run%20Maven%20Tests/badge.svg
    :alt: Github Workflow Tests Status

.. image:: https://github.com/qbicsoftware/core-utils-lib/workflows/QUBE%20lint/badge.svg
    :target: https://github.com/qbicsoftware/core-utils-lib/workflows/QUBE%20lint/badge.svg
    :alt: qube Lint Status

.. image:: https://readthedocs.org/projects/core-utils-lib/badge/?version=latest
    :target: https://core-utils-lib.readthedocs.io/en/latest/?badge=latest
    :alt: Documentation Status

.. image:: https://flat.badgen.net/dependabot/thepracticaldev/dev.to?icon=dependabot
    :target: https://flat.badgen.net/dependabot/thepracticaldev/dev.to?icon=dependabot
    :alt: Dependabot Enabled


Core Utilities Library - Collection of non-Vaadin, non-Liferay utilities.

* Free software: MIT
* Documentation: https://core-utils-lib.readthedocs.io.


Author
--------
Created by Luis de la Garza (luis.delagarza@qbic.uni-tuebingen.de), maintained and developed further by the [ITSS team](https://github.com/orgs/qbicsoftware/teams/itss) of QBiC.

Description
------------
Contains a collection of classes and utilities that depend neither on Vaadin nor on Liferay.

How to Install
-----------------

This is a library and the most common way to use this library in particular is by including it in your `pom.xml` as a dependency:

.. code-block:: xml

    <dependency>
      <groupId>life.qbic</groupId>
      <artifactId>core-utils-lib</artifactId>
      <version>X.Y.Z</version>
    </dependency>


Features
--------


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


Credits
-------

This project was created with qube_.

.. _qube: https://github.com/qbicsoftware/qube
