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

package guru.zoroark.tegral.niwen.lexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class OffsetCharSequenceTest {
    @Test
    fun offset_char_sequence_length() {
        assertEquals("hello".offsetBy(3).length, 2)
    }

    @Test
    fun offset_char_sequence_throws_error_if_incorrect_offset() {
        assertFails {
            "hey".offsetBy(4)
        }
    }

    @Test
    fun offset_char_sequence_correct() {
        val offset = "hello".offsetBy(2)
        assertEquals(offset[0], 'l')
        assertEquals(offset[1], 'l')
        assertEquals(offset[2], 'o')
        assertFailsWith<IndexOutOfBoundsException> { offset[3] }
    }

    @Test
    fun substring() {
        val offset = "Foo bar".offsetBy(3)
        assertEquals(" ba", offset.substring(0..2))
    }
}
