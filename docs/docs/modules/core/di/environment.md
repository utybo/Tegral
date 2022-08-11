---
sidebar_position: 3
---

# Injection Environment

An environment is a manager for multiple components that can inject one another. They are created using the `tegralDi` DSL function, onto which you can call `put` functions to add components:

```kotlin
val environment = tegralDi {
    // There are multiple possible styles for adding components:

    // 1. Using a lambda
    put { RepositoryA() }
    put { Controller(scope) }

    // 2. Using a constructor reference
    put(::RepositoryB)
    put(::Service)
}
```

Note that these all use reified types, but `KClass` alternatives are available if the types are not known at compile-time:

```kotlin
val environment = tegralDi {
    // 1. Using a lamda
    put(RepositoryA::class) { RepositoryA() }
    put(Controller::class) { Controller(scope) }

    // 2. Using a constructor reference
    put(RepositoryB::class, ::RepositoryB)
    put(Service::class, ::Service)
}
```

You may also add [qualifiers](./qualifiers.md) to your components:

```kotlin
val environment = tegralDi {
    // A. With reified types
    // 1. Using a lambda
    put(named("my repo")) { RepositoryA() }
    put(named("my controller")) { Controller(scope) }

    // 2. Using a constructor reference
    put(named("another repo"), ::RepositoryB)
    put(named("my service"), ::Service)

    // B. With class objects
    // 1. Using a lambda
    put(RepositoryA::class, named("my repo")) { RepositoryA() }
    put(Controller::class, named("my controller")) { Controller(scope) }

    // 2. Using a constructor reference
    put(RepositoryB::class, named("another repo"), ::RepositoryB)
    put(Service::class, named("my service"), ::Service)
}
```

Once your environment is created, you may retrieve objects from it directly, e.g. to start a server or a service somewhere:

```kotlin
val environment = tegralDi {
    // ...
}

environment.get<Service>().start()
// Or, using a qualifier
environment.get<Service>(named("my service")).start()
```
