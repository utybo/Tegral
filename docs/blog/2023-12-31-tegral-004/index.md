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

## Tegral OpenAPI improvement

### Set common operation properties in OpenAPI paths

That title probably does not make any sense, so let's dig in a little:

OpenAPI documents are made of paths (e.g. `/foo/bar`), which themselves contain operations (e.g. `GET`, which would make `GET /foo/bar`).

Previously, you could only define operation attributes (descriptions, responses, headers, etc.) on the operation. That ended up being somewhat cumbersome as all operations in a single path tend to have some similar details (e.g. two operations under `/cat/{id}` will both have information on the `id` parameter).

Moreover, as a @Ribesg pointed out [in a GitHub issue](https://github.com/utybo/Tegral/issues/59), that ended up making things like properly defining multiple operations on a single Ktor resource impossible. More specifically, Ktor resources provide an *operation* description while they actually really only represent *paths*. That meant that you could not define multiple descriptions for the same resource.

You can now define properties that should be present on all operations of a path directly in the path. For example:

```kotlin
// Before
"/cat/{id}" {
    get {
        description = "Get the cat with the given ID"

        "id" pathParameter {
            description = "The ID of the cat"
            // ...
        }
    }

    put {
        description = "Update the cat with the given ID"

        "id" pathParameter {
            description = "The ID of the cat"
            // ...
        }
    }

    // ...
}

// After
"/cat/{id}" {
    "id" pathParameter {
        "The ID of the cat"
    }

    get {
        description = "Get the cat with the given ID"
    }

    put {
        description = "Update the cat with the given ID"
    }

    // ...
}
```

`describeResource` now accepts a `PathDsl` instead of an `OperationDsl`, and you can now do things like this:

```kotlin
@Serializable @Resource("/cat/{id}")
class Cat(val id: Long) {
    companion object : OpenApiDescription by describeResource({
        "id" pathParameter {
            description = "The ID of the cat"
        }

        get {
            description = "Get the cat with the given ID"
        }

        put {
            description = "Update the cat with the given ID"
        }
    })
}
```

### Cascading OpenAPI descriptions for Ktor resources

Ktor resources' descriptions will now *cascade*. Anything defined at the path level of an outer resource will be replicated in the inner resource, e.g.:

```kotlin
@Resource("/cat") @Serializable
class Cat {
    companion object : ResourceDescription by describeResource({
        tags += "cat-api"
    })

    @Resource("/{id}") @Serializable
    class WithId(val id: Int) {
        companion object : ResourceDescription by describeResource({
            // Inherits the tags += "cat-api"
            "id" pathParameter {
                description = "The ID of the thing"
            }
        })
    }

    @Resource("/owner")
    @Serializable
    class OwnerDetails(val parent: WithId) {
        companion object : ResourceDescription by describeResource({
            // Inherits the tags += "cat-api" as well as the "id" path parameter
            get {
                summary = "Retrieve the owner details of the cat"
            }
        })
    }
}
```

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

## Misc. changes

Here are smaller, miscellaneous changes.

### Tegral Config: Fully optional sectioned configuration

Tegral Config's sectioned configurations are now fully optional if **all** of the defined sections are optional.

### Tegral OpenAPI: Add headers in responses

It's in the spec but I forgot to add it, woopsies. This has been fixed and you can use `"Hello" header { }` in your responses!
