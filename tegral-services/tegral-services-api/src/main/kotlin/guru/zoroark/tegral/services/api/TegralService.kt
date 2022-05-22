package guru.zoroark.tegral.services.api

interface TegralService {
    suspend fun start()
    suspend fun stop()

    val name get() = javaClass.name
    fun getHealth(): HealthStatus = HealthStatus.NotMonitored
}
