package guru.zoroark.tegral.web.appdefaults

import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertFalse

class KeepAliveServiceTest {
    @Test
    fun `Keep alive start stop properly stops scope`(): Unit = runBlocking {
        val kas = KeepAliveService()
        withTimeout(500) {
            launch {
                kas.start()
                kas.stop()
            }
            launch {
                // If the scope is not stopped properly, this will hang and cause a timeout.
                kas.scope.coroutineContext.job.join()
            }
        }
        assertFalse(kas.scope.isActive)
    }
}
