package guru.zoroark.tegral.services.api

/**
 * A service within a Tegral DI environment (and possibly a Tegral application).
 *
 * A Tegral application will always `start` then later `stop` a service exactly once, but Tegral DI-based applications
 * that manage services themselves may do what they please.
 */
interface TegralService {
    /**
     * Starts this service. Blocking in this function is not safe: consider using `withContext(Dispatchers.IO)` if you
     * need to make blocking calls.
     */
    suspend fun start()

    /**
     * Stops this service. Blocking in this function is not safe: consider using `withContext(Dispatchers.IO)` if you
     * need to make blocking calls.
     */
    suspend fun stop()
}
