# Fundefs

:::warning Experimental

This feature is **experimental**. Its API may be changed or removed at any time without it being considered a breaking change.

:::

Fundefs (short for **fun**ctional component **def**initions) allow you to define components as functions. The parameters of the function will be automatically injected from the environment. Here's a simple example:

```kotlin
class Greeter {
    fun greet(name: String) = "Hello, $name!"
}

@Fundef
fun greetAlice(greeter: Greeter): String {
    return greeter.greet("Alice")
}

val env = tegralDi {

    put(::Greeter)
    put(::greetAlice)
}

val fundef = env.getFundefOf(::greetAlice)
val result = fundef.invoke()
// result == "Hello, Alice!"
```

## `putFundef` vs `put`

There are currently two ways of adding fundefs to an environment: `putFundef` and `put`.

As part of the experimentation process, we are slowly transitioning from `putFundef` to the regular `put` function. You need to use the `@Fundef` annotation on your fundefs for them to be properly recognized at the moment. For example, either of these forms will work:

1. Using an annotated function and `put`

    ```kotlin
    @OptIn(ExperimentalFundef::class)
    @Fundef
    fun myFundef(greeter: Greeter): String {
        // ...
    }

    val env = tegralDi {
        // ...
        put(::myFundef)
    }
    ```

2. Using `putFundef`

    ```kotlin
    fun myFundef(greeter: Greeter): String {
        // ...
    }

    val env = tegralDi {
        // ...
        putFundef(::myFundef)
    }
    ```

## Overriding fundef parameters

You can override parameters at invoke-time if necessary. To do this, provide a map with parameter name as keys and the value to use as values.

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
// highlight-start
val result = fundef.invoke(mapOf("greeter" to Greeter()))
// highlight-end
```

## Extension functions

You can also use extension functions as fundefs. Note that the extension receiver (in the example, the `Greeter` class) will **never** be injected by Tegral DI. You will have to provide an instance when calling `invoke()`. All other parameters are injected as normal.

```kotlin
class Greeter {
    fun greet(name: String) = "Hello, $name!"
}

fun Greeter.greetAlice() = greet("Alice")

val env = tegralDi {
    putFundef(Greeter::greetAlice)
}

val fundef = env.getFundefOf(::greetAlice)
val result = fundef.invoke(extension = Greeter())
```

The same goes for instance functions.

```kotlin
class Greeter {
    fun greet(name: String) = "Hello, $name!"
}

class AliceGreeter {
    fun greetAlice(greeter: Greeter): String {
        return greeter.greet("Alice")
    }
}

val env = tegralDi {
    put(::Greeter)
    putFundef(AliceGreeter::greetAlice)
}
val fundef = env.getFundefOf(AliceGreeter::greetAlice)
val result = fundef.invoke(instance = AliceGreeter())
```

## Using qualifiers

By default, when injecting parameters, Tegral DI will try to inject something that has a matching type and an empty qualifier. You can tweak this to use a different qualifier via the `configureFundef` function:

```kotlin
class Greeter {
    fun greet(name: String) = "Hello, $name!"
}

fun greetAlice(greeter: Greeter): String {
    return greeter.greet("Alice")
}

val env = tegralDi {
    put(named("I greet people"), ::Greeter)
    // highlight-start
    putFundef(
        ::greetAlice.configureFundef {
            "greeter" qualifyWith named("I greet people")
        }
    )
    // highlight-end
}

val fundef = env.getFundefOf(::greetAlice)
val result = fundef.invoke()
// result == "Hello, Alice!"
```

## Checking if a call is valid

You can call `checkCallable` on the fundef to verify if the call will fail or not:

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
// highlight-start
fundef.checkCallable() // ok
fundef.checkCallable(mapOf("greeter" to Greeter())) // ok
fundef.checkCallable(mapOf("greeter" to "not a greeter")) // throws an exception
// highlight-end
```
