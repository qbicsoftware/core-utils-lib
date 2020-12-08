package life.qbic.services.connectors

import groovy.json.JsonSlurper

import life.qbic.services.Service
import life.qbic.services.ServiceConnector
import life.qbic.services.ServiceType

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ConsulConnector implements ServiceConnector, AutoCloseable {

    URL registryUrl

    HttpClient client

    ConsulConnector(URL serviceRegistry) {
        this.registryUrl = serviceRegistry
        this.client = HttpClient.newHttpClient()
    }

    @Override
    List<Service> searchServicesForType(ServiceType type) {
        def serviceNames = new ServiceNames()
        def serviceList = []
        def registryServiceResponse = queryRegistryForService(serviceNames.serviceNameForType.get(type))
        registryServiceResponse.each { Map response ->
            def service = new Service(type, new URL(response["ServiceAddress"] as String))
            serviceList << service
        }
        return serviceList
    }

    private List<Map> queryRegistryForService(String name) {
        URI uri = URI.create("${registryUrl.toExternalForm()}/catalog/service/$name")

        HttpRequest request = HttpRequest.newBuilder().uri(uri).build()
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString())

        def serviceMap = new JsonSlurper().parseText(response.body()) as List<Map>
        return serviceMap
    }

    @Override
    void close() throws Exception {
        client = null
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
