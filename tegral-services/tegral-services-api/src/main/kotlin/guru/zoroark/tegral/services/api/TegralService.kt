package guru.zoroark.tegral.services.api

interface TegralService {
    suspend fun start()
    suspend fun stop()
}
