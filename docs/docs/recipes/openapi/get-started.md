# Get started with Tegral OpenAPI

Tegral OpenAPI is a set of libraries that allow interoperation between [OpenAPI](https://www.openapis.org/), [Swagger UI](https://swagger.io/tools/swagger-ui/) and various Kotlin tools.

The exact way to get started depends on what you want to do:

- If you want to add OpenAPI/Swagger to a Tegral Web application, see [here](#tegral-web)
- If you want to add OpenAPI/Swagger to a Ktor application, see [here](#ktor)
- If you want to use a Kotlin syntax for writing OpenAPI specifications instead of YAML, see [here](#embedded-openapi-dsl)
- If you want to integrate an OpenAPI DSL to your application or library, see [here](#embedded-openapi-dsl)

## OpenAPI scripts

Tegral OpenAPI provides a scripting system for writing your OpenAPI definitions as Kotlin files. This is mostly for fun or if you quickly need to whip up an OpenAPI definition -- if your web framework allows it, you should probably use their OpenAPI integration instead.

Getting started is fairly easy: simply create a file that ends with `.openapi.kts` with your specs definition written using the Tegral OpenAPI DSL:

```kotlin title="spec.openapi.kts"
title = "My API"
version = "0.0.1"

"/" {
    get {
        description = "Get things"
        200 response {
            description = "It went well"
            plainText { schema<String>("Some response...") }
            // also valid: "text/plain" content { ... }
        }
    }
}
```

You can then use the Tegral OpenAPI CLI to compile and convert your script to JSON or YAML. The CLI itself is not available as a "standalone" executable. You should use a tool like JBang to execute the CLI, like so:

```bash
jbang run guru.zoroark.tegral:tegral-openapi-cli:VERSION --help
```

There are several options we will not detail here, but the gist is that you can turn your script into JSON via a command like this:

```bash
$ jbang run guru.zoroark.tegral:tegral-openapi-cli:VERSION spec.openapi.kts
[i] openapi.dump         - Compiling script...
[i] openapi.dump         - Evaluating script...
[!] compiler             - Using new faster version of JAR FS: it should make your build faster, but the new implementation is experimental
{"openapi":"3.0.1","info":{"title":"My API","version":"0.0.1"},"paths":{"/":{"get":{"description":"Get things","responses":{"200":{"description":"It went well","content":{"text/plain":{"schema":{"type":"string"},"example":"Some response..."}}}}}}}}
```

You can then copy-paste the result in [Swagger Editor](https://editor.swagger.io) to preview the results.

:::tip

On most UNIX-like shells (zsh, bash, etc.), you can set an alias to make this easier to run:

```bash
$ alias tegral-openapi="jbang run guru.zoroark.tegral:tegral-openapi-cli:VERSION"
$ tegral-openapi spec.openapi.kts
[i] openapi.dump         - Compiling script...
...
```

:::

## Embedded OpenAPI DSL

Tegral OpenAPI provides a DSL for writing OpenAPI specifications in Kotlin, which outputs [Swagger Core](https://github.com/swagger-api/swagger-core) objects. You can then serialize these objects to JSON or YAML via Swagger Core's built-in classes.

Refer to the documentation of [OpenAPI DSL](/modules/core/openapi/dsl.md) for more information.

## Ktor

Refer to [this page](/modules/core/openapi/ktor.md) for installation instructions.

## Tegral Web

Refer to [this page](/modules/core/openapi/tegral-web.md) for installation instructions.
