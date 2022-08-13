---
sidebar_position: 5
---

# Tegral Web feature

The Tegral Web feature automatically sets up OpenAPI support:

```kotlin
install(OpenApiFeature)
```

## Included in the box

When installing the OpenAPI support feature, you get:

- An OpenAPI endpoint at `/_t/openapi`
- Swagger UI served at `/_t/swagger`
- The ability to describe [the application](./ktor.md#describing-the-application) and its [endpoints](./ktor.md#describing-endpoints) via the `describe` functions.
