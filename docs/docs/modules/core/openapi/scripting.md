---
sidebar_position: 2
---

# Scripting

Tegral OpenAPI provides definitions and host utilities for [Kotlin scripting](https://github.com/Kotlin/KEEP/blob/master/proposals/scripting-support.md).

## Scripts

Tegral OpenAPI scripts are files that end in `.openapi.kts`. They have a readily accessible [`RootDsl`](./dsl.md#rootdsl) and can use all of the features of the [OpenAPI DSL](./dsl.md)

Here's an example of a file that defines a title, a version and a simple endpoint:

```kotlin title=myapi.openapi.kts
title = "My API"
version = "1.2.3"

"/hello" get {
    description = "Returns a greeting"
    200 response {
        description = "A greeting"
        plainText { schema("Hello World!") }
    }
}
```

## Consuming scripts

Scripts can be consumed in a few ways.

### Programmatically

If you wish to load a script within your own Kotlin application, you will need the `tegral-openapi-scripthost` library. This library provides a `OpenApiScriptHost` object with relevant functions -- refer to the KDoc comments for more information.

### Via a CLI

A CLI tool is available and allows you to convert your `.openapi.kts` script to a `.json` or `.yaml` file. Please refer to [this page](./cli.md) for more information.
