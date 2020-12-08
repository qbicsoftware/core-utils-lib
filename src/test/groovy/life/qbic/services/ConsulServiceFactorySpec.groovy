package life.qbic.services

import life.qbic.services.connectors.ConsulConnector
import spock.lang.Specification

/**
 * <class short description - 1 Line!>
 *
 * <More detailed description - When to use, what it solves, etc.>
 *
 * @since <versiontag>
 */
class ConsulServiceFactorySpec extends Specification {

    def "query QBiC's test registry must work"() {
        given:
        URL registryUrl = new URL("http://service-registry-test.am10.uni-tuebingen" +
                ".de:8080/registry/v1")
        ServiceConnector consulConnector = new ConsulConnector(registryUrl)

        when:
        List<Service> services = new ConsulServiceFactory(consulConnector)
                .getServicesOfType(ServiceType.SAMPLE_TRACKING)

        then:
        services.size() == 1
    }

}
