---
sidebar_position: 4
---

# Ktor plugins

Tegral OpenAPI provides two plugins for Ktor:

- A plugin for describing endpoints and serving OpenAPI JSON and YAML files.
- A plugin for serving [Swagger UI](https://swagger.io/tools/swagger-ui/) from your Ktor application.

## `tegral-openapi-ktor`

This plugin lets you add OpenAPI to your Ktor application, describe endpoints and serve OpenAPI JSON and YAML files.

### `TegralOpenApiKtor` installation

You can install the `TegralOpenApiKtor` plugin using the regular `install` syntax:

```kotlin
install(TegralOpenApiKtor)
```

### Describing the application

You can provide more information about your application by providing a configuration block when installing the plugin. In this configuration block, you have access to [`RootDsl`](./dsl.md#rootdsl) and can use all of its features.

Here's an example of setting a title, description and version:

```kotlin
install(TegralOpenApiKtor) {
    title = "My API"
    description = "This is my API"
    version = "1.2.3"
}
```

You can also call `describe` on the application directly to further configure the application:

```kotlin
decribe {
    termsOfService = "https://example.com"
}
```

### Describing endpoints

You can describe endpoints using the `describe` function right after it:

```kotlin
routing {
    get("/hello") {
        call.respondText("HEllo World!")
    } describe {
        description = "Returns a greeting"
        200 response {
            description = "A greeting"
            plainText { schema("Hello World!") }
        }
    }
}
```

You get access to everything in [`OperationDsl`](./dsl.md#operationdsl).

### Serving the OpenAPI file

You can setup an OpenAPI endpoint using the `openApiEndpoint(...)` function, passing the path from which the OpenAPI file will be served:

```kotlin
routing {
    get("/hello") {
        // ...
    } describe {
        // ...
    }

    openApiEndpoint("/openapi")
}
```

This endpoint has a few available options:

- The format of the output can be specified via the `format` parameter.
  - `json` will output the OpenAPI JSON file (default).
  - `yaml` will output the OpenAPI YAML file.
- The version of the OpenAPI specification can be specified via the `version` parameter.
  - `3.0` will output a 3.0 specification (default).
  - `3.1` will output a 3.1 specification.

For example, calling `/openapi?format=yaml&version=3.0` would output the 3.0 OpenAPI specification using YAML.

### Retrieving the OpenApi object

You can also retrieve the `OpenApi` object programmatically by retrieving the OpenAPI plugin object and calling `buildOpenApiDocument()` on it. For example, here's an endpoint that responds with the title of the OpenAPI document:

```kotlin
routing {
    get("/title") {
        val openapi = application.openApi.buildOpenApiDocument()
        call.respondText(openapi.info.title)
    }
}
```

## `tegral-openapi-ktorui`

The `TegralSwaggerUiKtor` plugin allows you to serve Swagger UI right from your Ktor application.

The Swagger UI plugin is independent from the OpenAPI plugin. In case you are retrieving an OpenAPI document from somewhere else, you can just use the Swagger UI plugin.

### `TegralSwaggerUiKtor` installation

First, install the plugin:

```kotlin
install(TegralSwaggerUiKtor)
```

Then, set up the Swagger UI endpoint within your `routing` block:

```kotlin
routing {
    swaggerUiEndpoint(path = "/swagger", openApiPath = "/openapi")
}
```

The first parameter is the path from which you wish to serve Swagger UI. The second parameter is the path from which the OpenAPI document will be loaded.

:::warning

Due to limitations in Ktor, you must call `swaggerUiEndopint` from the **root** of the `routing` block. Nesting it in another route will lead to faulty redirections.

:::
