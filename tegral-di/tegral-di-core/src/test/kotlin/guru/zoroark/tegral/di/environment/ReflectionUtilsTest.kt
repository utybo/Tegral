package guru.zoroark.tegral.di.environment

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ReflectionUtilsTest {
    class Unrelated

    open class Grandparent
    open class Parent : Grandparent()
    class Child : Parent()
    object ChildButObject : Parent()

    @Test
    fun `ensureInstance, same class`() {
        assertDoesNotThrow { ensureInstance(Parent::class, Parent()) }
    }

    @Test
    fun `ensureInstance, subclass`() {
        assertDoesNotThrow { ensureInstance(Parent::class, Child()) }
    }

    @Test
    fun `ensureInstance, subclass (object)`() {
        assertDoesNotThrow { ensureInstance(Parent::class, ChildButObject) }
    }

    @Test
    fun `ensureInstance, expect child but get parent`() {
        assertThrows<IllegalArgumentException> { ensureInstance(Parent::class, Grandparent()) }
    }

    @Test
    fun `ensureIsntance, expect child but get unrelated`() {
        assertThrows<IllegalArgumentException> { ensureInstance(Parent::class, Unrelated()) }
    }
}
