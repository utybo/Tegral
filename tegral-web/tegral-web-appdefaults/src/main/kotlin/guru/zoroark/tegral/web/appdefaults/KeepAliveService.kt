package guru.zoroark.tegral.web.appdefaults

import guru.zoroark.tegral.services.api.TegralService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * A simple service that keeps the JVM running.
 */
class KeepAliveService : TegralService {
    private val executor = Executors.newSingleThreadExecutor()
    private val dispatcher = executor.asCoroutineDispatcher()
    private val scope = CoroutineScope(dispatcher)

    override suspend fun start() {
        scope.launch { awaitCancellation() }
    }

    override suspend fun stop() {
        scope.cancel()
        dispatcher.close()
    }
}
