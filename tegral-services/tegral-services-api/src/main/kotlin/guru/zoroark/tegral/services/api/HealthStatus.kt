package guru.zoroark.tegral.services.api

sealed class HealthStatus(val message: String?) {
    class Healthy(message: String?): HealthStatus(message)
    class Unhealthy(message: String, cause: Throwable?): HealthStatus(message)
    class Degraded(message: String): HealthStatus(message)
    class Unknown(message: String): HealthStatus(message)

    object NotMonitored : HealthStatus(null)
}
