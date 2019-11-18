package life.qbic.services

class Service {

    ServiceType type

    URL rootUrl

    URL healthEndpoint

    URL routesEndpoint

    Service(ServiceType type, URL rootUrl) {
        this.type = type
        this.rootUrl = rootUrl
        this.healthEndpoint = new URL(this.rootUrl.toExternalForm() + '/health')
        this.routesEndpoint = new URL(this.rootUrl.toExternalForm() + '/routes')
    }

}
