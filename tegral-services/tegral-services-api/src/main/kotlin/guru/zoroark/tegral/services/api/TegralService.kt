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
