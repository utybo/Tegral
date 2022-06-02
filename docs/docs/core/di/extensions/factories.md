# Factories

:::caution

This extension is experimental.

:::

*This is a pure extension that is compatible with all environments.*

Many frameworks allow you to inject two kinds of components:

- *Singletons*, where a single object instance is injected wherever needed
- *Factories* (also known as transient), where any component that depends on it gets its own instance of the object.

Factories are very useful for objects like loggers or to get more advanced behavior depending on the requesting component.

All injections made within Tegral DI are singletons -- there is no such thing as a transient injection. The factory extension provides a wrapper over Tegral DI's singleton mechanism to replicate the factory behavior found in other frameworks.

Tegral DI factories are an extension of Tegral DI's system -- in fact, they are implemented entirely with public APIs from Tegral DI's core system!

## Usage

You can create factories using the `putFactory` method within your environment builder or within a module.

```kotlin
val myModule = tegralDiModule {
    putFactory { Foo() }
}

val myEnvironment = tegralDi {
    putFactory { Bar() }
}
```

Factories are injected using the `scope.factory()` syntax. For example, requesting the `Bar` factory will look like this:

```kotlin
class INeedBar(scope: InjectionScope) {
    val bar: Bar by scope.factory()
}
```

Here is a full example using loggers:

```kotlin
class Logger {
    fun logInfo(message: String) = println("INFO: $message")
    fun logWarn(message: String) = println("WARN: $message")
}

class ServiceA(scope: InjectionScope) {
    private val logger: Logger by scope.factory()

    fun doSomething() {
        logger.logInfo("Doing something in A...")
    }
}

class ServiceB(scope: InjectionScope) {
    private val logger: Logger by scope.factory()
    private val a: ServiceA by scope()

    fun doSomething() {
        a.doSomething()
        logger.logInfo("Doing something in B...")
    }
}

val environment = tegralDi {
    putFactory { Logger() }

    put(::ServiceA)
    put(::ServiceB)
}

environment.get<ServiceA>().doSomething()
// INFO: Doing something in A...
// INFO: Doing something in B...
```


:::caution

Make sure you do not use `by scope()` to retrieve an object that is supposed to be created by a factory!

:::

### Requestor-dependent object creation

It is possible to create objects based on which component is requesting them. The `putFactory` block gives the requesting object as a parameter, which can then use to do anything.

This is useful for giving a logger a name, for example:

```kotlin
class Logger(private val name: String) {
    fun logInfo(message: String) = println("($name) INFO: $message")
    fun logWarn(message: String) = println("($name) WARN: $message")
}

// ServiceA and ServiceB are the same as before

val environment = tegralDi {
    putFactory { requester -> Logger(requester::class.qualifiedName ?: "<anon>") }

    put(::ServiceA)
    put(::ServiceB)
}

environment.get<ServiceA>().doSomething()
// (org.example.ServiceA) INFO: Doing something in A...
// (org.example.ServiceB) INFO: Doing something in B...
```

You can go even further with annotations. Here's a fuller example that uses annotations to name loggers, with an additional fallback mechanism:

```kotlin
// The annotation's definition
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoggerName(val name: String)

// Reflection logic used to retrieve the name the logger of a class.
private val KClass<*>.loggerName: String
    get() = findAnnotation<LoggerName>()?.name ?: qualifiedName ?: "<anon>"

// Our actual Logger object: nothing really special here...
class Logger(private val name: String) {
    fun logInfo(message: String) = println("($name) INFO: $message")
    fun logWarn(message: String) = println("($name) WARN: $message")
}

// Let's give ServiceA a custom name...
@LoggerName("Custom logger name!")
class ServiceA(scope: InjectionScope) {
    private val logger by scope.factory()

    fun doSomething() {
        logger.logInfo("Doing something in A...")
    }
}

// ... but let's also leave ServiceB as is.
class ServiceB(scope: InjectionScope) {
    private val logger by scope.factory()
    private val a by scope()

    fun doSomething() {
        a.doSomething()
        logger.logInfo("Doing something in B...")
    }
}

// Let's now create our environment:
val environment = tegralDi {
    putFactory { requester -> Logger(requester::class.loggerName) }

    put(::ServiceA)
    put(::ServiceB)
}

// And test our logger:
environment.get<ServiceA>().doSomething()
// (Custom logger name!) INFO: Doing something in A...
// (org.example.ServiceB) INFO: Doing something in B...
```

## Factories under the hood.

In a nutshell, *factory objects* are injected in the environment, and this factory gets invoked when the object is requested. The factory object is injected in the environment as a regular singleton.

Factories are objects which implement the functional interface `InjectableFactory<T>` (the Kotlin equivalent for SAMs in Java). Its `make(requester: Any): T` function is invoked when an object is requested.

Factories are injected with an additional qualifier. Because of its generic typing, only including the class would lead to components with the same identifier, which would be `InjectableFactory::class` without qualifiers. In order to avoid this, an `InjectableFactoryOutputTypeQualifer` is used as a qualifier to differentiate between factories. The `outputs` function can also be used as an alias for this qualifier.

The `putFactory` method is in charge of the entire injection process: creating the factory with the correct qualifier and injecting it within a simple `put` call.

On the injection side, the only change is that a wrapper is put around the `scope()` call: instead of the actual type being requested, the corresponding factory is requested when using `scope.factory()`. It is then wrapped using the `WrappedReadOnlyProperty` class which executes the factory's `make` method and wrapped *again* within a `SynchronizedLazyPropertyWrapper`. As such, the factory's method is only called once and only when necessary.
