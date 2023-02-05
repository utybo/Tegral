---
title: "[DRAFT] Tegral 0.0.4 release"
description: Features, bug fixes, updates, hooray!
slug: tegral-0-0-4-release
authors:
  - name: utybo
    title: Maintainer
    url: https://github.com/utybo
    image_url: https://github.com/utybo.png
tags: [release]
# image: 
draft: true
---

Long time no see! Welcome to the release notes for Tegral 0.0.4!

<!-- truncate -->

## Updated dependencies

We have a few updated dependencies in this release, but most importantly **Ktor was updated to verison 2.2.0**, which introduces some breaking changes. Refer to [their migration guide](https://ktor.io/docs/migrating-2-2.html) if you use:

- Cookie response configuration
- `call.request.origin.host` or `port`
- Persistence w.r.t. caching in Ktor Client

Here's the full list of upgrades:

| Dependency   | Version change   |
| ------------ | ---------------- |
| Hoplite      | 2.5.2 -> 2.7.1   |
| MockK        | 1.12.5 -> 1.13.4 |
| Ktor         | 2.1.0 -> 2.2.3   |
| Swagger Core | 2.2.2 -> 2.2.8   |
| Kotlin       | 1.7.10 -> 1.8.10 |
| JUnit        | 5.9.0 -> 5.9.2   |
| Swagger UI   | 4.13.2 -> 4.15.5 |
| Logback      | 1.2.11 -> 1.4.5  |
