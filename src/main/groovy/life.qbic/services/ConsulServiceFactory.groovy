package life.qbic.services

class ConsulServiceFactory {

    private final ServiceConnector connector

    ConsulServiceFactory(ServiceConnector connector){
        this.connector = connector
    }

    Service getServiceOfType(ServiceType type) {
        return new Service()
    }


}