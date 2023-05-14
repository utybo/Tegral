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

package guru.zoroark.tegral.prismakt.generator.tests

import org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS

fun prismaDbPush(connectionUrl: String?) {
    val res = ProcessBuilder()
        .apply {
            val npx = if (IS_OS_WINDOWS) "npx.cmd" else "npx"
            command(npx, "prisma", "db", "push", "--force-reset", "--skip-generate")
            if (connectionUrl != null) environment()["DATABASE_URL"] = connectionUrl
            inheritIO()
        }
        .start()
        .waitFor()
    require(res == 0) { "'prisma db push' failed" }
}
