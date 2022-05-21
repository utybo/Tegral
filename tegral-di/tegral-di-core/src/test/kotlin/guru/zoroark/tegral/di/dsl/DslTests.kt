package guru.zoroark.tegral.di.dsl

import guru.zoroark.tegral.di.ExampleClass
import guru.zoroark.tegral.di.ExampleClass2
import guru.zoroark.tegral.di.InvalidDeclarationException
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.ScopedContext
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.named
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DslTests {
    @Test
    fun `Building from single-element builder works (without warnings)`() {
        val supplier: ScopedContext.() -> ExampleClass = { ExampleClass() }
        val env = EnvironmentContextBuilderDsl().apply {
            put(supplier)
        }
        val built = env.build()
        assertEquals(1, built.declarations.size)
        assertEquals(Identifier(ExampleClass::class), built.declarations.get<ExampleClass>().identifier)
    }

    @Test
    fun `Building from multi-elements builder works`() {
        val supplier: ScopedContext.() -> ExampleClass = { ExampleClass() }
        val supplier2: ScopedContext.() -> ExampleClass2 = { ExampleClass2() }
        val env = EnvironmentContextBuilderDsl().apply {
            put(supplier)
            put(supplier2)
        }
        val built = env.build()
        assertEquals(2, built.declarations.size)
        assertEquals(Identifier(ExampleClass::class), built.declarations.get<ExampleClass>().identifier)
        assertEquals(Identifier(ExampleClass2::class), built.declarations.get<ExampleClass2>().identifier)
    }

    @Test
    fun `Building from constructor references works`() {
        class NoConstructor
        class GoodConstructor(val scope: InjectionScope)

        EnvironmentContextBuilderDsl().apply {
            put(::NoConstructor)
            put(::GoodConstructor)
        }.build()
    }

    @Test
    fun `Duplicate via inferred type put should throw error`() {
        val ex = assertThrows<InvalidDeclarationException> {
            EnvironmentContextBuilderDsl().apply {
                put { ExampleClass() }
                put { ExampleClass() }
            }
        }
        assertEquals(
            "Duplicate identifier: Tried to put 'guru.zoroark.tegral.di.ExampleClass (<no qualifier>)', " +
                "but one was already present",
            ex.message
        )
    }

    @Test
    fun `Duplicate via class put should throw error`() {
        val ex = assertThrows<InvalidDeclarationException> {
            EnvironmentContextBuilderDsl().apply {
                put(ExampleClass::class) { ExampleClass() }
                put(ExampleClass::class) { ExampleClass() }
            }
        }
        assertEquals(
            "Duplicate identifier: Tried to put 'guru.zoroark.tegral.di.ExampleClass (<no qualifier>)', but " +
                "one was already present",
            ex.message
        )
    }

    @Test
    fun `Duplicate via class and inferred type put should throw error`() {
        val ex = assertThrows<InvalidDeclarationException> {
            EnvironmentContextBuilderDsl().apply {
                put(ExampleClass::class) { ExampleClass() }
                put { ExampleClass() }
            }
        }
        assertEquals(
            "Duplicate identifier: Tried to put 'guru.zoroark.tegral.di.ExampleClass (<no qualifier>)', but " +
                "one was already present",
            ex.message
        )
    }

    @Test
    fun `Named and unnamed qualifiers should not throw error`() {
        open class TheSuperclass
        class TheClass : TheSuperclass()

        val context = EnvironmentContextBuilderDsl().apply {
            put(::TheClass)
            put(named("using-ctor"), ::TheClass)
            put(named("using-lambda")) { TheClass() }
            put(TheSuperclass::class, named("using-ctor-and-kclass"), ::TheClass)
            put(TheSuperclass::class, named("using-lambda-and-kclass")) { TheClass() }
        }.build()
        assertEquals(context.declarations.size, 5)
        assertEquals(
            context.declarations.keys,
            setOf(
                Identifier(TheClass::class),
                Identifier(TheClass::class, named("using-ctor")),
                Identifier(TheClass::class, named("using-lambda")),
                Identifier(TheSuperclass::class, named("using-ctor-and-kclass")),
                Identifier(TheSuperclass::class, named("using-lambda-and-kclass"))
            )
        )
    }

    @Suppress("RedundantNullableReturnType")
    fun noArgButReturnsNullable(): String? = error("hello")

    @Test
    fun `Invalid put function reference (nullable return type)`() {
        val ex = assertThrows<InvalidDeclarationException> {
            @Suppress("UNCHECKED_CAST")
            val function: KFunction<String> = ::noArgButReturnsNullable as KFunction<String>
            tegralDi {
                put(String::class, function)
            }
        }
        val message = assertNotNull(ex.message)
        assertContains(message, "nullable return type")
    }

    @Suppress("UNUSED_PARAMETER")
    fun oneArgNotScope(str: String): String = error("That shouldn't happen")

    @Test
    fun `Invalid put function reference (invalid single argument)`() {

        val ex = assertThrows<InvalidDeclarationException> {
            tegralDi {
                put(String::class, ::oneArgNotScope)
            }
        }
        val message = assertNotNull(ex.message)
        assertContains(message, "must take either no arguments")
    }

    @Suppress("UNUSED_PARAMETER")
    fun twoArgs(scope: InjectionScope, str: String): String = error("That shouldn't happen")

    @Test
    fun `Invalid put function reference (too many arguments)`() {
        val ex = assertThrows<InvalidDeclarationException> {
            tegralDi {
                put(String::class, ::twoArgs)
            }
        }
        val message = assertNotNull(ex.message)
        assertContains(message, "must take either no arguments")
    }
}
