package life.qbic.services

interface Service {

    URL getRootUrl()

    URL getHealtEndpoint()

    URL getRoutesEndpoint()

    boolean isAlive()
}
