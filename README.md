# <img src="assets/tegral_logo.svg" alt="Tegral logo" width=32> Tegral

Tegral is an opinionated collection of Kotlin frameworks, libraries, helpers and DSLs that help you make awesome apps, from web back-ends and beyond!

> ⚡ Tegral is in an experimental status. Please report any issue you may find!

```kotlin
class HelloController : KtorController() {
    override fun Routing.install() {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}

fun main() {
    tegral {
        put(::HelloController)
    }
}
```

Each Tegral library is reusable and extensible. Pick the most suitable libraries for your project, or maybe even all of them for a full Tegral-based application.
