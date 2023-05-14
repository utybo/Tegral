# <img src="assets/tegral_logo_v2.png" alt="Tegral logo" width=32> Tegral

![GitHub Workflow Status (CI)](https://img.shields.io/github/actions/workflow/status/utybo/Tegral/ci.yaml?label=CI&logo=github&style=for-the-badge) [![Apache 2 license](https://img.shields.io/badge/License-Apache%202-lightgray?style=for-the-badge)](LICENSE) [![Latest release](https://img.shields.io/github/v/release/utybo/Tegral?color=purple&include_prereleases&label=Latest%20release&style=for-the-badge)](https://github.com/EpiLink/EpiLink/releases) [![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg?style=for-the-badge)](https://ktlint.github.io/)

**Tegral** is a collection of reusable Kotlin components, like libraries and frameworks, that can be used by themselves or combined together to create back-ends apps with Tegral Web.

[**DOCUMENTATION**](https://tegral.zoroark.guru) | [**CHANGELOG**](CHANGELOG.md)

> ⚡ Tegral is in an alpha status. Please report any issue you may find!

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
