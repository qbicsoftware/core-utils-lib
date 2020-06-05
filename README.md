# Core Utilities Library
[![Build Status](https://travis-ci.com/qbicsoftware/core-utils-lib.svg?branch=master)](https://travis-ci.com/qbicsoftware/core-utils-lib)
[![Release](https://img.shields.io/github/v/release/qbicsoftware/core-utils-lib)](https://github.com/qbicsoftware/core-utils-lib/tags)
[![Code Coverage]( https://codecov.io/gh/qbicsoftware/core-utils-lib/branch/master/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/core-utils-lib)

Core Utilities Library - Collection of non-Vaadin, non-Liferay utilities.

## Author
Created by Luis de la Garza (luis.delagarza@qbic.uni-tuebingen.de), maintained and developed further by the ITSS team of QBiC.

## Description
Contains a collection of classes and utilities that depend neither on Vaadin nor on Liferay.

## How to Install
This is a library and the most common way to use this library in particular is by including it in your `pom.xml` as a dependency:

```xml
<dependency>
  <groupId>life.qbic</groupId>
  <artifactId>core-utils-lib</artifactId>
  <version>X.Y.Z</version>
</dependency>
```

## Features

### Find QBiC services

Finding QBiC service instances from within an application is as easy as this:

```Groovy
// Example in Groovy
def serviceList = []
def serviceRegistryUrl = new Url("https://host-name-of-registry:<port>/v1")
def connector = new ConsulConnector(serviceRegistryUrl)
connector.withCloseable {
    ConsulServiceFactory factory = new ConsulServiceFactory(it)
    serviceList.addAll(factory.getServicesOfType(ServiceType.SAMPLE_TRACKING))
}
```


```Java
// Example in Java
List serviceList = new ArrayList<>()
Url serviceRegistryUrl = new URL("https://host-name-of-registry:<port>/v1")
try (ConsulConnector connector = new ConsulConnector(serviceRegistryUrl)) {
    ConsulServiceFactory factory = new ConsulServiceFactory(connector)
    serviceList.addAll(factory.getServicesOfType(ServiceType.SAMPLE_TRACKING))
}
```
