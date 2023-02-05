# Add Start/Stop services

Applies to [Tegral Web](/modules/web/index.md) and [Tegral DI Services](/modules/core/di/extensions/services.md).

Tegral DI Services has an internal mechanism for any component that needs to perform actions when started and/or stopped. This mechanism can be used by any component registered in Tegral DI (including anything you `put` in a Tegral Web application).

## The Tegral DI Services extension

:::tip

Skip to the next section if you use Tegral Web.

:::

Services are powered by an _extension_ of Tegral DI. This extension is automatically installed if you use Tegral Web, but you will need to install it yourself if you use Tegral DI stand-alone.

Add a `useServices()` call to your Tegral DI block, e.g.:

```kotlin
tegralDi {
    useServices()

    // ...
}
```

Moreover, you will need to manually instruct Tegral DI when to actually start or stop components. You will need to call the `.env.startAll()` and `.env.stopAll()` functions whenever appropriate.

```kotlin
val env = tegralDi {
    useServices()
    // ...
}
env.services.startAll()
// At this point, Tegral DI has called `start` on all registered service components
// ...
env.services.stopAll()
```

:::tip

Because services use suspending functions, you'll need to use a coroutine to start and stop your services. If you are not running `startAll` or `stopAll` from a coroutine already, you can use the following to start and stop services instead:

```kotlin
runBlocking { env.services.startAll() }
// ...
runBlocking { env.services.stopAll() }
```

:::

## Services

Declaring services is done by implementing `TegralDiService` in a component.

Here's an example:

```kotlin
class MyFakeService : TegralDiService {
    override suspend fun start() {
        println("Starting service!")
    }

    override suspend fun stop() {
        println("Stopping service!")
    }
}
```

To register this service, `put` it like any other component:

```kotlin
// With standalone Tegral DI
tegralDi {
    useServices()
    put(::MyFakeService)
}

// With Tegral Web
tegral {
    put(::MyFakeService)
}
```

For more information on what is available with Tegral DI Services, see [this page](/modules/core/di/extensions/services.md).
