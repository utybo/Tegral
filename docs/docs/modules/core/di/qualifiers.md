# Qualifiers

Components must be uniquely identified. By default, components are identified by their type. However, you can add qualifiers to your components to distinguish them if they have the same type.

For example, you can inject two lists within your environment: a list of banned users and a list of admin users:

```kotlin
class BanRepository {
    fun ban(user: String) {
        // ...
    }
}

class BanService(scope: InjectionScope) {
    private val permaBannedUsers: List<String> by scope(named("banned"))
    private val adminUsers: List<String> by scope(named("admin"))

    private val banRepo: BanRepository by scope()

    fun ban(userToBan: String, requester: String) {
        if (requester in adminUsers && requester !in permaBannedUsers) {
            banRepo.ban(userToBan)
        } else {
            error("Not enough permissions!")
        }
    }
}

val environment = tegralDi {
    put(::BanRepository)
    put(::BanService)

    put(named("banned")) { listOf("banned1", "banned2") }
    put(named("admin")) { listOf("admin1", "admin2") }
}

environment.get<BanService>().ban("banned3", "admin1")
```

## Standard qualifiers

Tegral DI provides a few standard qualifiers for your convenience:

### EmptyQualifier

The empty qualifier (`EmptyQualifier`) is the default qualifier that is used if you do not specify any qualifier.

### named

Named qualifiers allow you to qualify your components using a string.

This qualifier can be created using the `named()` function:

```kotlin
class A

class B(scope: InjectionScope) {
    private val a: A by scope(named("this is a"))
}

val env = tegralDi {
    put(named("this is a"), ::A)
    put(::B)
}
```

### typed

Due to type erasure, components are only identified by their "base" class. For example, if you were to register a `List<String>` like so...

```kotlin
put<List<String>> { listOf("one", "two", "three") }
```

The actual identifier would actually only have a reference to `List` and not `List<String>`.

This is fine for most cases, but can lead to unexpected duplicate component exceptions.

```kotlin
// Will not work!
put<List<String>> { listOf("one", "two", "three") }
put<List<Int>> { listOf(1, 2, 3) }
```

If you do need access to full type information, you can use the `typed()` qualifier:

```kotlin
put<List<String>>(typed<List<String>>()) { listOf("one", "two", "three") }
put<List<Int>>(typed<List<Int>>()) { listOf(1, 2, 3) }

class A(scope: InjectionScope) {
    private val listStr: List<String> by scope(typed<List<String>>())
    private val listInt: List<Int> by scope(typed<List<Int>>())
}
```

Note that the `typed` qualifier does not actually check whether the value is of the correct type. It is only useful for "tagging" the component and claim that it is of the specified type, which is used "as-is".

## Combining qualifiers

You can combine multiple qualifiers using the `+` operator.

```kotlin
put<List<Int>>(named("digits") + typed<List<Int>>()) { listOf(1, 2, 3) }
put<List<Int>>(named("tens") + typed<List<Int>>()) { listOf(10, 20, 30) }
put(::Example)

class Example(scope: InjectionScope) {
    private val digits: List<Int> by scope(named("digits") + typed<List<Int>>())
    private val tens: List<Int> by scope(named("tens") + typed<List<Int>>())
}
```

## Custom qualifiers

You can create your own qualifiers if you wish. Qualifiers are just objects that:

- Implement the `Qualifier` interface
- Override *at least* the `toString`, `equals` and `hashCode` methods.

Your qualifiers can be data classes to ease the process. Additionally, like standard qualifiers, you can provide a more DSL-ish way to create qualifiers with a function that does something like:

```kotlin
@TegralDsl
fun myQualifier(someArg: String) = MyQualifier(someArg)
```

For example, we could create an integer-based qualifier like so:

```kotlin
data class NumberedQualifier(val value: Int) : Qualifier

@TegralDsl
fun numbered(value: Int) = NumberedQualifier(value)

// Here's how we can use our qualifier:

class A

class B(scope: InjectionScope) {
    private val a: A by scope(numbered(42))
}

val env = tegralDi {
    put(numbered(42), ::A)
    put(::B)
}
```
