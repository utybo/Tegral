# Tegral DI

Tegral DI, formerly known as Shedinja, is an easy-to-use dependency injection framework for Kotlin, mainly inspired by [Koin](https://insert-koin.io) with the objective of being more flexible and safer.

- Tegral DI components are easy to test
- Tegral DI does not rely on any global state, meaning that environments can be ran entirely in parallel, both during tests and at runtime.
- You can automatically test your Tegral DI modules for incoherences, cyclic dependencies, completeness and much more.
- Tegral DI does not use code generation and is a pure Kotlin/JVM library.

```kotlin
// A simple repository/service/controller sample using Tegral DI

class Repository {
    // ...
}

class Service(scope: InjectionScope) {
    private val repository: Repository by scope()

    // ...
}

class Controller(scope: InjectionScope) {
    private val service: Service by scope()

    // ...
}

val module = tegralDiModule {
    put(::Repository)
    put(::Service)
    put(::Controller)
}

val env = tegralDi {
    put(module)
}
```

:::note

You do not need to use `tegralDi` when running full Tegral applications (i.e. when using AppDSL). The actual environment is managed for you.

:::

## Differences with Shedinja

Tegral DI is the successor of (and a fork of) [Shedinja](https://shedinja.zoroark.guru). The main differences are:

- Tegral DI's services extension is integrated with Tegral CoreServices instead of using its own service interface.
- Tegral provides a more powerful way of interfacing DI with Ktor via the Tegral Controllers library.
- Shedinja provides Koin 2 and Koin 3 libraries that have not been ported to Tegral.

You can safely migrate to Tegral DI without many issues (some classes and functions have been renamed) if you do not use the Koin integration nor the Ktor extension.