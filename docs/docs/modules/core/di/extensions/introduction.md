# Extensions

Tegral DI's core APIs are designed to be quite simple yet flexible. However, due to their simple nature, they do not necessarily allow you to do everything you want.

In addition to the core APIs (described within the "Usage" and "Testing" sections of this documentation website), Tegral DI also provides more advanced features in the form of extensions.

Official extensions are all located within the `guru.zoroark.tegral.di.extensions` package and its sub-packages.

## Kinds of extensions

There are two (main) kinds of extensions:

- **Pure extensions.** These are pure wrappers over existing APIs and will work with any injection environment. Most extensions that are about adding special kinds of components are probably pure extensions that wrap the existing putting and injection mechanisms into something more user-friendly. An example of such an extension is the [factories extension](./factories.md).

- **Installable extensions.** These extensions provide additional functionality aside from the regular put-and-inject workflow and require their own mechanisms to be stored within the environment. They are only compatible with extensible injection environments.

:::tip

If an environment is extensible, it should mention it somewhere in its documentation. The default injection environment (which is used when you use `tegralDi {` instead of `tegralDi(Something) {`) supports extensions.

:::

## Extensions behind the scenes

This section will specifically talk about installable extensions. Pure extensions are just functions that wrap existing functions: they do not hook themselves into Tegral DI engines in any special way. Installable extensions are more interesting.

Extensible injection environments provide additional capabilities:

- They host a meta-environment, an environment that hosts components from extensions.
- They call back components of the meta-environment when things happen.

### Meta-environment

Installable extensions do their work by injecting themselves into the *meta-environment*. This special environment is embedded within extensible injection environments. It is a sub-environment dedicated to hosting components from extensions.

Extensions will generally:

* Inject themselves within the meta-environment.
* Get called back by the extension engine when something special happens (e.g. the environment is initialized).
* Wrap the meta-environment in some way when using them (e.g. when starting all services with the [services extension](./services.md)).

### Tags

Declarations (`put(...)` calls) can be *tagged* using declaration tags. These declaration tags provide metadata for extensions that would like additional information attached to individual declarations.

```kotlin
tegralDi {
    put(::SomeComponent) with someTag
}
```

Tags can be processed by any component installed within the meta-environment of an extensible environment by implemented the `DeclarationsProcessor` interface.

### DSL

For consistency, extensions should use the following entry points:

For installation, provide a `useXXX()` function that is an extension of `ExtensibleContextBuilderDsl` and adds components to the meta-environment using the `meta { }` function.

Tags should either be simple value definitions or a function that only takes a lambda (allowing for additional DSL if required). A possible pattern for extensions that provide a set of tags that do not require additional DSL is to do something similar to the services extension:

```kotlin
// For use when processing the tags
enum class MyExtensionTags : DeclarationTag {
    TagOne,
    TagTwo,
}

// For use in the DSL
val someMoreObviousDslishName = TagOne
val somethingElse = TagTwo
```

