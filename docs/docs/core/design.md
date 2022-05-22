---
sidebar_position: 2
---

# Design of Core Libraries

There are *many* libraries available in Tegral, with some being more or less reliant on one another.

*Tegral components* are sets of libraries that provide a specific functionality (for example, `tegral-di` is a component), while a library is an actual artifact you can get (e.g. libraries from `tegral-di` like `tegral-di-core`, `tegral-di-ktor`, etc.).

## Component purpose

Each component and library is classified by its purpose.

- **Foundation** components and libraries are standalone and do not rely on any other component (apart from standard Kotlin libraries, such as `kotlin-stdlib`, `kotlin-reflect` or `kotlinx-cororutines`). They provide value on their own and can be used as-is.
- **Extension** components and libraries *extend* other libraries (be they from Tegral or from a third party), adding functionality to them.
- **Integration** components aim to make multiple libraries coexist together and/or make them easier to use.

## Library dependencies

Tegral libraries can have a number of dependencies, both on other libraries from Tegral, the Kotlin standard libraries and third party libraries. In order to simplify users who wish to only use a single Tegral component and not everything

- **Minimal:** only relies on the standard Kotlin libraries.
- **Light:** relies on the standard libraries and on some other foundation Tegral libraries.
- **Medium:** relies on the standard libraries, possibly some other foundation or extension Tegral libraries and/or third party libraries.
- **Heavy:** relies on many Tegral libraries and third party libraries of all kinds. These libraries generally assume that you are building a full Tegral-based application.

Ideally, most libraries should fall in the minimal and light categories, some in the medium one while very few are in the heavy one.
