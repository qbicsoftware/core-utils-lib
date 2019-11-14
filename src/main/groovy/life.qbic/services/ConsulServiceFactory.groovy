package life.qbic.services

class ConsulServiceFactory {

    private final URL registry

    ConsulServiceFactory(URL registry){
        this.registry = registry
    }

    Service getServiceOfType(ServiceType type) {
        return new SampleTrackingService()
    }

}