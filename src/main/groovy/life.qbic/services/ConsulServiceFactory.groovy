package life.qbic.services

class ConsulServiceFactory {

    private final ServiceConnector connector

    ConsulServiceFactory(ServiceConnector connector){
        this.connector = connector
    }

    List<Service> getServicesOfType(ServiceType type) {
        def services = connector.searchServicesForType(type)
        return services
    }


}