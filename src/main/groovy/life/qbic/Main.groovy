package life.qbic

import life.qbic.services.ConsulServiceFactory
import life.qbic.services.Service
import life.qbic.services.ServiceConnector
import life.qbic.services.ServiceType
import life.qbic.services.connectors.ConsulConnector

/**
 * <class short description - 1 Line!>
 *
 * <More detailed description - When to use, what it solves, etc.>
 *
 * @since <versiontag>
 */
class Main {

    public static void main(String[] args) {
        URL registryUrl = new URL("http://service-registry-test.am10.uni-tuebingen" +
                ".de:8080/registry/v1")
        ServiceConnector consulConnector = new ConsulConnector(registryUrl)
        List< Service> services = new ConsulServiceFactory(consulConnector)
                .getServicesOfType(ServiceType.SAMPLE_TRACKING)
        services.each {
            println it.rootUrl
        }
    }
}
