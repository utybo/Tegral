package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectableModule
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

/**
 * Base class for testing Tegral DI-applications.
 *
 * The basic idea behind this class is to provide testing facilities around a single "subject". Let's say we wanted to
 * test a service and its interactivity with a repository. Our subject here would be the service.
 *
 * The class itself provides a wrapper around an [UnsafeMutableEnvironment] that is well-suited for test scenarios.
 *
 * Here is what a typical use of this class could look like, with the MockK library for mocking our repository.
 *
 * ```kotlin
 * // Main code
 * interface Repository {
 *     fun storeThis(text: String)
 * }
 *
 * class Service(scope: InjectionScope) {
 *     private val repository: Repository by scope()
 *
 *     fun incomingText(text: String) {
 *         // ...
 *         repository.storeThis(text)
 *         // ...
 *     }
 * }
 *
 * // Test code
 * class TestService : TegralDiBaseTest<Service>(::Service) {
 *     @Test
 *     fun `Accepts incoming text properly`() = test {
 *         put<Repository> {
 *             mockk { every { storeThis("hello") } just runs }
 *         }
 *
 *         subject.incomingText("hello")
 *
 *         verify { get<Repository>().storeThis("hello") }
 *     }
 * }
 * ```
 *
 * The class defines the environment using a *base module* that usually only contains the test subject's entry, but may
 * also contain additional dependencies as you see fit. Refer to the different constructors for more information.
 *
 * @constructor This constructor takes a pre-built base module and the class of the subject and uses them as a base.
 * @param subjectClass The class of the subject, used for [subject] to work properly.
 * @param baseModule The base module as an [InjectableModule] instance.
 */
open class TegralDiBaseTest<S : Any>(
    private val subjectClass: KClass<S>,
    private val baseModule: InjectableModule
) {
    /**
     * This constructor takes a module builder and the subject's class, builds the module then uses that as a base.
     *
     * This is equivalent to calling `tegralDiModule` on the builder and running the primary constructor instead.
     *
     * For example:
     *
     * ```
     * class TestService : TegralDiBaseTest<Service>(
     *     Service::class, { put(::Service) }
     * ) {
     *     // ...
     * }
     * ```
     *
     * @param subjectClass The class of the subject, used for [subject] to work properly.
     * @param baseModuleBuilder The base module as a builder, just like the body of a [tegralDiModule] call.
     */
    constructor(subjectClass: KClass<S>, baseModuleBuilder: ContextBuilderDsl.() -> Unit) : this(
        subjectClass, tegralDiModule("<base test module>", baseModuleBuilder)
    )

    /**
     * Shortcut for cases where you want to create a single-component module. For example, this:
     *
     * ```
     * class TestService : TegralDiBaseTest<Service>(::Service)
     * ```
     *
     * Is equivalent to this:
     *
     * ```
     * class TestService : TegralDiBaseTest<Service>(Service::class, { put(::Service) })
     * ```
     */
    @Suppress("UNCHECKED_CAST")
    constructor(constructor: KFunction<S>) : this(
        constructor.returnType.jvmErasure as KClass<S>,
        { put(constructor.returnType.jvmErasure as KClass<S>, constructor) }
    )

    /**
     * Create a new environment from this instance's base module (and an optional [additionalBuilder]) and execute
     * the [block] within it.
     *
     * See the example [on this class][TegralDiBaseTest]
     */
    @TegralDsl
    open fun <T> test(
        additionalBuilder: ContextBuilderDsl.() -> Unit = {},
        block: UnsafeMutableEnvironment.() -> T
    ): T {
        val env = tegralDi(UnsafeMutableEnvironment) {
            put(baseModule)
            additionalBuilder()
        }
        return with(env) { block() }
    }

    /**
     * Returns the subject of this test class within this environment.
     */
    val UnsafeMutableEnvironment.subject: S
        get() = this.get(Identifier(subjectClass))
}
