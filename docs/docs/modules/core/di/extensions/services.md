# Services support

*This is an installable extension that is only compatible with extensible environments.*

The services extension provides an easy way to "start and stop all components". The exact meaning of "starting components" is up to you: it could be connecting to a database, starting a web server, or anything else.

It is somewhat equivalent to [hosted services on ASP.NET Core](https://docs.microsoft.com/en-us/aspnet/core/fundamentals/host/hosted-services). Note that Tegral DI services are not autostarted: you need to call `env.services.startAll()` manually.

:::note

In Tegral Web appplications, starting and stopping is managed by the application's environment. You should not call `env.services.startAll()` yourself.

:::

## Usage

Note that this extension is not included in the base release of Tegral DI due to its use of coroutines. You need to add `guru.zoroark.tegral:tegral-di-services` in your dependencies.

### Installation

This extension needs to be installed with `useServices()`, like so:

```kotlin
val env = tegralDi {
    useServices()

    // ...
}
```

If you are building a full Tegral application where [features](/modules/core/featureful/index.mdx) can be installed, consider installing the [Tegral Services feature](/modules/core/featureful/index.mdx) instead.

### Creating services

Services are regular components that implement `TegralDiService`.

Here is an example using a (fictional) database system:

```kotlin
class DatabaseService(scope: InjectionScope) : TegralDiService {
    private val db by scope<DatabaseConfiguration>() wrapIn { Database(it) }

    override suspend fun start() {
        db.connect()
    }

    override suspend fun stop() {
        db.disconnect()
    }
}
```

You can then `put` this component like any other component:

```kotlin
val env = tegralDi {
    useServices()

    put(::DatabaseService)
}
```

### Starting and stopping services

Once your environment is created, you can then call `.services.startAll()` and `.services.stopAll()` to start and stop all services.

```kotlin
val env = tegralDi {
    useServices()

    put(::DatabaseService)
}

env.services.startAll()
// ...
env.services.stopAll()
```

Note that `startAll` and `stopAll` are suspending functions. If you wish to start/stop your services from some non-coroutine code, wrap the call in `runBlocking { ... }` like so:

```kotlin
runBlocking { env.services.startAll() }
// ...
runBlocking { env.services.stopAll() }
```

### Statistics

The Tegral DI services extension provides information on how much time each service took to start/stop. There are two ways to access this information:

#### Message handler

The `startAll` and `stopAll` functions optionally take a lambda. This lambda takes a `String` and is used by the extension to send messages when services are done starting/stopping.

By default, this lambda is a no-op. You can supply your own lambda or function to this parameter. For example, using a regular `println` call:

```kotlin
env.services.startAll(::println)
env.services.stopAll(::println)
```

#### Return value

`startAll` and `stopAll` return a map of identifiers to the time it took to start the component with this identifier. You can use this map to process the statistics in any way you like.

### Excluding services

You may exclude services from being started/stopped by [tagging](./introduction.md#tags) their `put` statement with `noService`, `noServiceStart` or `noServiceStop`.

```kotlin
val env = tegralDi {
    useServices()

    put(::DoNotStartMe) with noServiceStart
    put(::DoNotStopMe) with noServiceStop
    put(::DoNotStartNorStopMe) with noService
}
```
