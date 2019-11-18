package life.qbic.services

interface ServiceConnector {

    /**
     * Given a list of keywords, the method will return matching services.
     *
     * @param serviceType The service type to search for
     * @return A list of matching services
     */
    List<Service> searchServicesForType(ServiceType serviceType)

}