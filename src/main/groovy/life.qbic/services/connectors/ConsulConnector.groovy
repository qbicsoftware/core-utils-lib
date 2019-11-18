package life.qbic.services.connectors

import groovy.json.JsonSlurper
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.uri.UriBuilder
import life.qbic.services.Service
import life.qbic.services.ServiceConnector
import life.qbic.services.ServiceType


class ConsulConnector implements ServiceConnector, AutoCloseable {

    URL registryUrl

    RxHttpClient httpClient

    ConsulConnector(URL serviceRegistry) {
        this.registryUrl = serviceRegistry
        this.httpClient = RxHttpClient.create(serviceRegistry)
    }

    @Override
    List<Service> searchServicesForType(ServiceType type) {
        def serviceNames = new ServiceNames()
        def serviceList = []
        def registryServiceResponse = queryRegistryForService(serviceNames.serviceNameForType.get(type))
        registryServiceResponse.each { response ->
            def service = new Service(type, response.ServiceAddress)
            serviceList << service
        }
        return serviceList
    }

    private Map queryRegistryForService(String name) {
        String uri = UriBuilder.of("${registryUrl.toExternalForm()}/catalog/service/{name}")
                               .expand(Collections.singletonMap("name", name))
                               .toString()
        def result = this.httpClient.toBlocking().retrieve(uri)
        def serviceMap = new JsonSlurper().parseText(result) as Map
        return serviceMap
    }

    @Override
    void close() throws Exception {
        httpClient.close()
    }

    class ServiceNames {

        Map<ServiceType, String> serviceNameForType = new EnumMap<>(ServiceType.class)

        ServiceNames() {
            serviceNameForType.put(ServiceType.SAMPLE_TRACKING, "sampletracking")
            serviceNameForType.put(ServiceType.WORKFLOW_TRACKING, "flowstore" )
            serviceNameForType.put(ServiceType.VARIANT_STORE, "variantstore" )
        }
    }

}
