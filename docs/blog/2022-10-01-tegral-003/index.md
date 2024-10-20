---
title: Tegral 0.0.3 release
description: Just a big ol' bunch of bug fixes and improvements
slug: tegral-0-0-3-release
authors: utybo
tags: [release]
image: zhao-chen-ag-RTLJgy54-unsplash.jpg
---

Tegral 0.0.3 fixes a few things from the 0.0.2 updates. It also adds a new integration for Ktor resources.

<!-- truncate -->

![An image of a foggy mountain scenery](zhao-chen-ag-RTLJgy54-unsplash.jpg)

This release is much less packed than [the 0.0.2 version](/2022-08-14-tegral-002/index.md) but features something new: a community contribution! On a personal note, I never really expected to see PRs from other people -- I knew that my previous project [Koa](https://github.com/utybo/Koa) (which [Tegral OpenAPI](pathname:///docs/modules/core/openapi/) replaces) gained some traction, or at least enough for people to say it should [be updated](https://github.com/utybo/Koa/issues/4).

Without further ado, let's have a look at some highlights from the changelog!

## Integration with Ktor Resources

Thanks to [@serras](https://github.com/serras)' contribution, you can now use Tegral OpenAPI with [Ktor's type-safe routing feature (aka resources)](https://ktor.io/docs/type-safe-routing.html).

To get started, add the new `tegral-openapi-ktor-resources` package. You'll then be able to describe endpoints like this:

```kotlin
@Resource("/hello/{name}")
@Serializable
class Hello(val name: String) {
    companion object : OpenApiDescription by describeResource({
        description = "Returns a greeting"
    })
}
```

This is currently just a shortcut for embedding OpenAPI descriptions in your resources file, which feels more natural.

Have a look at the [documentation](pathname:///docs/modules/core/openapi/ktor#integration-with-resources) for more information.

## Bug fixes

This release mostly includes small bug fixes:

- You can now use Tegral artifacts with Java 11+ instead of the previous 17+!

- Properly deprecate the `scope.factory()` syntax and the various `wrapIn` utility functions from Tegral DI. This syntax is useless since [the introduction of the new resolution system](../2022-08-14-tegral-002/index.md#more-flexible-injections-in-tegral-di) and I just forgot to deprecate it (oops).

- Tegral OpenAPI generated `null` values for examples instead of letting Swagger do its thing and generate one, which led to confusing OpenAPI and Swagger documents. This has been fixed, and OpenAPI no longer generates examples by default.

- In Tegral Web, the `RootConfig` was improperly exposed to the DI environment, which led to weird acrobatics being required to retrieve the configuration. This has been fixed and you can now access your configuration objects via its class or via `RootConfig`.
