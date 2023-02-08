---
title: "[DRAFT] Tegral 0.0.4 release"
description: Features, bug fixes, updates, hooray!
slug: tegral-0-0-4-release
authors:
  - name: utybo
    title: Maintainer
    url: https://github.com/utybo
    image_url: https://github.com/utybo.png
tags: [release]
# image: 
draft: true
---

Long time no see! Welcome to the release notes for Tegral 0.0.4!

<!-- truncate -->

## Optional configuration in Tegral Web

When using Tegral Web, you previously had to always specify a configuration file. This is no longer required, as Tegral Web will now use a default configuration if none is provided. This default configuration is to use port 8080 and bind against `0.0.0.0`.

## Tegral Config: Fully optional sectioned configuration

Tegral Config's sectioned configurations are now fully optional if **all** of the defined sections are optional.

## Experimental: Fundefs in Tegral DI

Fundefs allow you to define components as functions. Previously, you could only define components as classes that use properties to inject dependencies. Fundefs allow you to define components as functions. Here's a simple example:

```kotlin
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

You may not immediately see the usefulness of this, as this is a somewhat limited way of grabbing dependencies, but this will be extremely useful for Tegral Web Controllers. The end goal is to have a syntax that looks like this:

```kotlin
class Greeter {
    fun greet(name: String) = "Hello, $name!"
}

fun Routing.hello(greeter: Greeter) {
    get("/hello") {
        call.respondText(greeter.greet("Alice"))
    }
}

fun Application.myModule(/* ... */) {
  // ...
}

fun main() {
    tegral {
        put(::Greeter)
        put(Routing::hello)
        put(Application::myModule)
    }
}
```

Not only is this more concise, this is also much closer to Ktor's "module" concept, making it less confusing for those who are familiar with Ktor. This is *not* fully done yet, we're about a third of the way there. You can check out [this issue](https://github.com/utybo/Tegral/issues/65) to follow how it's going.

## Updated dependencies

We have a few updated dependencies in this release, but most importantly **Ktor was updated to verison 2.2.0**, which introduces some breaking changes. Refer to [their migration guide](https://ktor.io/docs/migrating-2-2.html) if you use:

- Cookie response configuration
- `call.request.origin.host` or `port`
- Persistence w.r.t. caching in Ktor Client

Here's the full list of upgrades:

| Dependency   | Version change   |
| ------------ | ---------------- |
| Hoplite      | 2.5.2 -> 2.7.1   |
| MockK        | 1.12.5 -> 1.13.4 |
| Ktor         | 2.1.0 -> 2.2.3   |
| Swagger Core | 2.2.2 -> 2.2.8   |
| Kotlin       | 1.7.10 -> 1.8.10 |
| JUnit        | 5.9.0 -> 5.9.2   |
| Swagger UI   | 4.13.2 -> 4.15.5 |
| Logback      | 1.2.11 -> 1.4.5  |
