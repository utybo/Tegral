---
sidebar_position: 2
---

# DI Checks

Tegral DI provides a check system for your modules that allow you to verify some of its properties.

Add a test anywhere within your test sources (preferably in its own class for clarity) with the following format:

```kotlin
@Test
fun `Tegral DI modules checks`() = tegralDiCheck {
    modules(/* ... */)

    // ...
}
```

You will need to pass your application's modules to the `modules(...)`, and add any checks you want to perform below them.

When a check fails, it throws an exception with a message that describes the problem, hereby making the test fail.

In order to use this feature "idiomatically", you should

- Define your modules in your main code in a public way. Note that a module itself does not instantiate its components: it only describes *how* to instantiate them. The environment created when calling `tegralDi` is the one responsible for that.
- Use a single test for all of the checks.


## Complete environment

:::note

Add this check by putting `complete()` after the modules declaration(s).

:::

Checks that the module set is complete, meaning that no dependency requirement is missing. For example, considering the following classes:

```kotlin
class A
class B(scope: InjectionScope) {
    val a: A by scope()
}
class C(scope: InjectionScope) {
    val b: B by scope()
    val a: A by scope()
}
```

This is correct:

```kotlin
val module = tegralDiModule {
    put(::A)
    put(::B)
    put(::C)
}

tegralDiCheck {
    modules(module)

    complete()
}
```

But this is not, as `C` has a dependency on `B`, which is missing in the module:

```kotlin
val module = tegralDiModule {
    put(::A)
    put(::C)
}

tegralDiCheck {
    modules(module)

    complete()
}
```

When this check fails, Tegral DI will tell you exactly which dependency is missing and which components requested it:

```kotlin
class A

class B(scope: InjectionScope) {
    private val a: A by scope()
    private val d: D by scope()
}

class C(scope: InjectionScope) {
    private val b: B by scope()
    private val d: D by scope()
    private val e: E by scope()
}

class D
class E

val module = tegralDiModule {
    put(::A)
    put(::B)
    put(::C)
}

tegralDiCheck {
    modules(module)

    complete()
}
```

```
Some dependencies were not found. Make sure they are present within your module definitions.
--> org.example.tegral.di.D (<no qualifier>) not found
    Requested by:
    --> org.example.tegral.di.B (<no qualifier>)
    --> org.example.tegral.di.C (<no qualifier>)
--> org.example.tegral.di.E (<no qualifier>) not found
    Requested by:
    --> org.example.tegral.di.C (<no qualifier>)
```

Note that the `complete` check does not verify if modules are complete on their own. It verifies that the entire module set is complete, meaning that, despite `B` being missing from `module1`, the fact that it can still be found in `module2` makes this system complete:

```kotlin
val module1 = tegralDiModule {
    put(::A)
    put(::C)
}

val module2 = tegralDiModule {
    put(::B)
}

tegralDiCheck {
    modules(module1, module2)

    complete()
}
```

## No cyclic dependencies

:::note

Add this check by putting `noCycle()` after the modules' declaration(s).

:::

Checks that the module set does not contain any cyclic dependencies. A cyclic dependency situation is when a class `Foo` depends on itself, either directly (`Foo` depends on `Foo` by injecting itself) or indirectly (`Foo` depends on `Bar` and `Bar` depends on `Foo`).

While cyclic dependency situations are generally handled correctly by Tegral DI, they can mean that you have a problem in your design somewhere, especially in cases of layered architecture.

Here is an example of a cyclic dependency that this check would report. A depends on B, which depends on A, hereby creating a cycle:

```kotlin
class A(scope: InjectionScope) {
    private val b: B by scope()
}

class B(scope: InjectionScope) {
    private val a: A by scope()
}

val module = tegralDiModule {
    put(::A)
    put(::B)
}

tegralDiCheck {
    modules(module)

    noCycle()
}
```

```
Cyclic dependency found:
    org.example.tegral.di.A (<no qualifier>)
--> org.example.tegral.di.B (<no qualifier>)
--> org.example.tegral.di.A (<no qualifier>)
Note: --> represents an injection (i.e. A --> B means 'A depends on B').
```

And here is a more complicated one:

```kotlin
class C(scope: InjectionScope) {
    private val d: D by scope()
}

class D(scope: InjectionScope) {
    private val e: E by scope()
}

class E(scope: InjectionScope) {
    private val f: F by scope()
    private val c: C by scope()
}

class F(scope: InjectionScope) {
    private val c: C by scope()
}

val module = tegralDiModule {
    put(::C)
    put(::D)
    put(::E)
    put(::F)
}

tegralDiCheck {
    modules(module)

    noCycle()
}
```

```
Cyclic dependency found:
    org.example.tegral.di.C (<no qualifier>)
--> org.example.tegral.di.D (<no qualifier>)
--> org.example.tegral.di.E (<no qualifier>)
--> org.example.tegral.di.F (<no qualifier>)
--> org.example.tegral.di.C (<no qualifier>)
Note: --> represents an injection (i.e. A --> B means 'A depends on B').
```

## Safe injection only

:::note

Add this check by putting `safeInjection()` after the modules decleration(s). **Due to safe injections being primordial in a Tegral DI system, you should put this rule at the top.**

:::

Checks that no object tries to perform an injection (i.e. actually retrieve a dependency) during their instantiation. Consider the following example of something you **should not do**:

```kotlin
class LogRepository {
    fun start() {
        // ...
    }
}

class Logger(scope: InjectionScope) {
    private val repo: LogRepository by scope()
    init {
        repo.start()
    }
}
```

This is incorrect because, internally, Tegral DI instantiates objects in the order they are declared. This is not a problem *as long as* no injection resolution is performed during the injection.

If we were to create an environment like so:

```kotlin
val env = tegralDi {
    put(::LogRepository)
    put(::Logger)
}
```

Everything would work fine, because, in the given order:

- `LogRepository` is instantiated and added to the environment's internal component map.
  - Object instantiation proceeds as normal: nothing happening there.
- `Logger` is instantiated and added to the environment's internal component map.
  - Object instantiation requests an injection handler via a delegated property via the `by scope()` syntax. The environment is called via the `scope` parameter and will provide an *injector* for the object. At this point, injection has not been performed yet: the actual `LogRepository` object has never been actually injected anywhere.
  - The `Logger` object *immediately* uses the repository, performing the actual injection now. In *this* case, it works fine, as the object is already present in the environment.

However, if we were to swap these two `put` statements around:

```kotlin
val env = tegralDi {
    put(::Logger)
    put(::LogRepository)
}
```

The faulty logic from above no longer works:

- `Logger` is instantiated and added to the environment's internal component map.
  - Object instantiation requests an injection handler just like before.
  - The `Logger` object *immediately* uses the repository, yet no repository has ever touched the environment. This would be similar to using a component that has never been injected if it weren't for the fact that, in this case, we are at a point where the object itself has not been actually injected within the environment's internal map*, leading to a broken environment state, and a usually undecipherable exception.

Here is an example of what the error message would look like for the example:

```
safeInjection' check failed.
The following injection is done during the instantiation of org.example.tegral.di.Logger (<no qualifier>):
    org.example.tegral.di.Logger (<no qualifier>)
--> org.example.tegral.di.LogRepository (<no qualifier>)

You *must not* actually perform injections during the instantiation of objects.
If you need to do something on an object provided by an environment before storing it as a property, use 'wrapIn' instead. See the documentation on the 'safeInjection' check for more details.
```

If you do need to perform some once-only action on the injected object you want to get, e.g. calling a function to create an object, you use the `wrapIn` utility function like so:

```kotlin
interface Cache
class CacheProvider {
    fun createCache(name: String): Cache {
        // ...
    }
}

// With manual dependency injection

class ServiceA(cacheProvider: CacheProvider) {
    private val cache = cacheProvider.createCache("serv_a_cache")
}

class ServiceB(cacheProvider: CacheProvider) {
    private val cache = cacheProvider.createCache("serv_b_cache")
}

// Equivalent with Tegral DI dependency injection

class ServiceA(scope: InjectionScope) {
    private val cache by scope<CacheProvider>() wrapIn { it.createCache("serv_a_cache") }
}

class ServiceB(scope: InjectionScope) {
    private val cache by scope<CacheProvider>() wrapIn { it.createCache("serv_b_cache") }
}
```

Note that while the injection may be done eagerly, lazily or actively, **the wrapping step is always done lazily**. The lambda is only executed once, and the value can no longer be changed, similar to how `lazy` from the standard library works. If you need more fine-grained control over the injection method (especially if you need the entire injection + transformation pipeline to be active), you should directly inject the object you are interested in within the environment. You can use [name qualifiers](../qualifiers.md) to differentiate between components of the same type.

## No unused component

:::note

Add this check by putting `noUnused()` after the modules declaration(s). Supports optional configuration.

:::

Unused components are components which are never injected anywhere within the components. For example, given this module:

```kotlin
class ServiceA(scope: InjectionScope) {
    private val b: ServiceB by scope()
}

class ServiceB(scope: InjectionScope) {
    private val c: ServiceC by scope()
}

class ServiceC

val module = tegralDiModule {
    put(::ServiceA)
    put(::ServiceB)
    put(::ServiceC)
}
```

The `ServiceA` component is considered unused, since it is not injected anywhere, and makes the `noUnused` check fail.

```
'noUnused' check failed.
The following component is not injected anywhere, making it unused.
--> org.example.tegral.di.ServiceA (<no qualifier>)

If some or all of the components mentioned above are still used outside of injections (e.g. via a 'get' call on the environment), you can exclude them from this rule by adding them after the 'noUnused':

    noUnused {
        exclude<ExcludeThis>()
        exclude<ExcludeThat>(named("exclude.that"))
        exclude(ExcludeIt::class)
        exclude(ExcludeMe::class, named("excluded"))
    }
```

:::caution

Note that an option-less `noUnused` succeeding means that [`noCycle`](#no-cyclic-dependencies) will fail. By definition, if all components can reach one another (i.e. the graph of components is strongly connected), it must have a cycle.

:::

### Excluding components from `noUnused`

A very frequent pattern is to have some sort of *entrypoint* within your injection modules. As this entrypoint will usually not be depended upon by any other dependency, it is retrieved via a `env.get<TheEntrypoint>()` on the environment created by the `tegralDi` function. As `noUnused` cannot detect such a use, you can exclude such entry-points (or any other component that is only retrieved via `get` and never injected) using the following syntax:

```kotlin
tegralDiCheck {
    modules(/* ... */)
  
    // Instead of
    noUnused()

    // Do
    noUnused {
        exclude<TheEntrypoint>()
        exclude<SomethingElse>(named("yes"))
    }
}
```

You will have to manually ensure that anything you manually exclude *is* in fact not useless.

You can exclude classes in the following ways:

```kotlin
// Reified, no qualifier
exclude<TheClass>()

// Reified, with qualifier
exclude<TheClass>(named("my class"))

// KClass, no qualifier
exclude(TheClass::class)

// KClass, with qualifier
exclude(TheClass::class, named("my class"))
```

:::caution

Excluding by specifying a class without a qualifier (`exclude<A>()` or `exclude(A::class)`) does **not** exclude all components of the given class no matter their qualifier: it only excludes the component with the given class and no qualifier.

:::
