package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.di.InvalidDeclarationException
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertNotSame

class FullTypeQualifierTest {
    class Container<T> {
        var value: T? = null
    }

    @Test
    fun `Test full type qualifier can inject two similarly typed`() {
        val env = tegralDi {
            put(typed<Container<List<String>>>()) { Container<List<String>>() }
            put(typed<Container<List<Int>>>()) { Container<List<Int>>() }
        }
        val strListContainer = env.get<Container<List<String>>>(typed<Container<List<String>>>())
        val intListContainer = env.get<Container<List<Int>>>(typed<Container<List<Int>>>())
        assertNotSame<Container<*>>(strListContainer, intListContainer)
    }

    @Test
    fun `Test full type qualifier fails with exact same type`() {
        assertThrows<InvalidDeclarationException> {
            tegralDi {
                put(typed<Container<List<String>>>()) { Container<List<String>>() }
                put(typed(typeOf<Container<List<String>>>())) { Container<List<String>>() }
            }
        }
    }

    @Test
    fun `Test full type qualifier does not fail with type projection`() {
        val env = tegralDi {
            put(typed<Container<List<String>>>()) { Container<List<String>>() }
            put(typed<Container<List<*>>>()) { Container<List<String>>() }
        }
        val strListContainer = env.get<Container<List<String>>>(typed<Container<List<String>>>())
        val projectedListContainer = env.get<Container<List<*>>>(typed<Container<List<*>>>())
        assertNotSame<Container<*>>(strListContainer, projectedListContainer)
    }
}
