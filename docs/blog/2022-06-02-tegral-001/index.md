---
title: Welcome to Tegral 0.0.1!
description: The first release of Tegral is now available!
slug: welcome-to-tegral-v0-0-1
authors: utybo
tags: [release]
image: damien-cornu-VzsixizA4c8-unsplash.jpg
hide_table_of_contents: false
---

Welcome to the first release of Tegral! Let's discuss what Tegral aims to be, give you a little context, and talk about what's already available. 👀

<!-- truncate -->

![Mountain](damien-cornu-VzsixizA4c8-unsplash.jpg)

Tegral is a collection of Kotlin libraries, DSLs and frameworks that aim to give you an awesome platform to build applications.

I had the idea of building a full framework like this while I was building yet another back-end in Kotlin. While all of the libraries in the ecosystem are really nice, I was really missing a solution that would just combine everything into a single ready-to-build package. While building your own stack is a fun experience, it's not exactly productive.

This journey first started with the creation of [Shedinja](https://github.com/utybo/Shedinja) (which has been integrated in Tegral as [Tegral DI](pathname:///docs/modules/core/di)), a super flexible dependency injection framework. After developing it for a while, I realized that I didn't need much else to get started building my own framework.

## Here comes Tegral

So here it is! Tegral has a few fundamental ideas behind it:

### Integrable-first

I.e., build everything expecting that people will want to use your libraries _without_ necessarily using the entire framework.

This is something that drove me crazy with [Quarkus'](https://quarkus.io) OIDC implementation. It had everything I needed, but was basically impossible to use without using a) the entire framework and b) in the ultra integrated way. That's a fine strategy, but I also find it extremely counter-productive, as it means you would have to reimplement logic you could just use cleanly from somewhere else.

This is one of the big design decisions behind Tegral: it's a collection of libraries _before_ being a framework. It just so happens that, when you combine everything, it gives you a really nice platform to build apps.

### No code generation

Code generation can be an extremely powerful and useful tool, but it also makes the build process more complicated, makes debugging harder, and in general just harms the developer experience when things go wrong. Tegral has 0 code generation and is just a bunch of regular old Maven dependencies.

### Testability

Tegral aims to make testing your application easy, transparent and extensible.

### Let developers dig deeper

From a high-level point of view, for the most part, Tegral really is just glue (or layers) on top of existing libraries like Ktor or Hoplite. A problem with this kind of layering is that you can easily reach a point where the layer does not give you access to what you need from the underlying framework. In order to combat this, Tegral makes it as easy as possible to let you access the relevant elements from the underlying libraries.

## Enough talking, what's available?

Currently, Tegral has quite a few elements:

- Documentation!
  - This very website
  - The [API](pathname:///dokka/index.html) documentation, made available using Dokka.
- Libraries!
  - And quite a lot of them! Just dig into the documentation to find out what the different components can all do for you.
  - Most notably, Tegral includes a [powerful DI framework called Tegral DI](pathname:///docs/modules/core/di) that is the successor to [Shedinja](https://shedinja.zoroark.guru).

## What's next?

There are a few things planned for Tegral:

- Write tutorials and record videos showcasing the current features of Tegral.
  - If you're curious about how to write Tegral applications right now, have a look at [AppDSL's documentation](pathname:///docs/modules/web/appdsl)
- Migrate other libraries I've made in the past to Tegral, namely:
  - [Ktor Rate Limit](https://github.com/utybo/ktor-rate-limit)
  - [Koa, an OpenAPI DSL and library for Kotlin/Ktor](https://github.com/utybo/Koa)
- Add more functionality, such as:
  - [Database integration with Exposed](https://github.com/jetbrains/exposed)
  - A database migration tool like Flyway, but with a 100% Kotlin/Exposed DSL.

I unfortunately do not have much time to work on my open-source projects these days. If you see anything you'd like to be added to Tegral, feel free to [open an issue](https://github.com/utybo/Tegral/issues) or maybe even contribute!
