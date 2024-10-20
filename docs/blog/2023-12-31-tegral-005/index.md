---
title: "Tegral 0.0.5 release"
description: 0.0.5 is now available!
slug: tegral-0-0-5-release
authors: utybo
tags: [release]
draft: true
image: steel-scaffolding-g85ea1a499_1280.jpg
---

Welcome to the release notes for Tegral 0.0.5!

<!-- TODO -->

<!-- truncate -->

## Fundefs, phase 2

:::note Experimental

Everything discussed in this section is experimental and requires the `@OptIn(ExperimentalFundef::class)` annotation.

:::

In the [0.0.4 release](../2023-05-14-tegral-004/index.md#experimental-fundefs-in-tegral-di), I announced the availability of _fundefs_ in Tegral DI. As a reminder, fundefs are a relatively low-level part of Tegral DI which allow you to create _functional component definitions_, like this one:

```kotlin title="Tegral 0.0.4"
class Greeter {
    fun greet(name: String) = "Hello, $name!"
}

fun greetAlice(greeter: Greeter): String {
    return greeter.greet("Alice")
}

val env = tegralDi {
    put(::Greeter)
    putFundef(::greetAlice)
}

val fundef = env.getFundefOf(::greetAlice)
val result = fundef.invoke()
// result == "Hello, Alice!"
```

Tegral 0.0.5 introduces 2 changes to the fundef system:

### Simple `put` for fundefs

You can annotate fundef functions with `@Fundef`, then `put` these functions instead of using `putFundef`.

This means that the above example can be rewritten like this:

```kotlin title="Tegral 0.0.5"
@Fundef
fun greetAlice(greeter: Greeter): String {
    return greeter.greet("Alice")
}

val env = tegralDi {
    put(::Greeter)
    put(::greetAlice)
}

val fundef = env.getFundefOf(::greetAlice)
val result = fundef.invoke()
// result == "Hello, Alice!"
```

The `@Fundef` _should_ be a temporary measure, and may be removed in the future if all goes according to plan.

### Tegral Web Controllers support

You can now write Ktor controllers and modules using fundefs! By adding the following to your `tegral {}` block:

```kotlin
tegral {
    // highlight-start
    install(WebControllersFeature) {
        enableFundefs = true
    }
    // highlight-end
}
```

You can now use simple fundefs to declare anything you want!

Here's a tiny example of a Ktor application using a fundef for a controller:

```kotlin
@file:OptIn(ExperimentalFundef::class)

@Fundef
fun Routing.myController() {
    get("/hello") {
        call.respondText("Hello, world!")
    }
}

fun app() = tegral {
    install(WebControllersFeature) {
        enableFundefs = true
    }

    put(Routing::controller)
}

fun main() {
    app()
}
```

Same goes for Ktor modules: here is an equivalent fundef using Ktor's `routing` block.

```kotlin
@Fundef
fun Application.myModule() {
    routing {
        get("/hello") {
            call.respondText("Hello, world!")
        }
    }
}
```

## Configurable features

Tegral Featureful features, which are the basic building block of Tegral applications, can now be configured _in-code_.

Previously, if you wanted to add configuration to your feature, you had to use _external configuration_, as in relying on a configuration file from a user. This can get quite cumbersome and limited quite quickly, and I believed we needed an alternative to simply declare some internal configuration that pretty much never changes based on some external configuration.

This is possible thanks to a significant redesign of the core `Feature` interfaces. In a nutshell, you can now configure compatible features (as in, features that _can_ be configured this way) like so:

```kotlin
tegral {
    install(MyFeature) {
        myProperty = "myValue"
    }
}
```

For more information on this, refer to the [Tegral Featureful document](/docs/modules/core/featureful#feature-types)

## Misc. breaking changes

The various variations of the `useConfiguration` functions available in the `tegral {}` block have been renamed. As a reminder, these functions allow you to further configure [Hoplite](https://github.com/sksamuel/hoplite) by changing the configuration class, adding property sources (i.e. adding config files), etc.

```kotlin
tegral {
    // Previously: useConfiguration<MyConfigClass> { ... }
    useConfigurationType<MyConfigClass> { ... }

    // Previously: useConfiguration(MyConfigClass::class) { ... }
    useConfigurationClass(MyConfigClass::class) { ... }

    // This syntax, without a type nor a class, does not change
    useConfiguration { ... }
}
```

## Dependency updates

Several dependencies have been updated, including an update from Kotlin 1.8.20 to Kotlin 1.9.
