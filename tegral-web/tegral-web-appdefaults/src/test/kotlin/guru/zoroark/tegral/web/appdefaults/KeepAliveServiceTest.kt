/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        withTimeout(3000) {
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
