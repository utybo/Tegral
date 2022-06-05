# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] (0.0.2)

### Added

- `tegral-di-test`
  - `UnsafeMutableEnvironment` is now an extensible environment. ([#16](https://github/utybo/Tegral/pull/16))
  - Added `entryOf` utility function. ([#16](https://github/utybo/Tegral/pull/16))
  - Utility classes for testing environments' behavior (`EnvironmentBaseTest`, `ExtensibleEnvironmentBaseTest` and `NotExtensibleEnvironmentBaseTest`). ([#16](https://github/utybo/Tegral/pull/16))([#16](https://github/utybo/Tegral/pull/16))
- `tegral-web-appdsl`
  - Added `TegralApplication.stop()` ([#19](https://github/utybo/Tegral/pull/19))

### Changed

- `tegral-di-core`
  - `tegralDi` now takes an additional, optional `metaEnvironmentKind` parameter when creating extensible environments. ([#16](https://github/utybo/Tegral/pull/16))
  - `InjectionEnvironmentKind<E>` interface is now a functional interface. ([#16](https://github/utybo/Tegral/pull/16))
  - `createMetaEnvironment` function now returns the correct type of `InjectionEnvironment`, determined via the generic parameter of `InjectionEnvironmentKind` ([#16](https://github/utybo/Tegral/pull/16))
- `tegral-web-appdsl`
  - `TegralApplication.start()` is now a suspending function.
- `tegral-web-controllers`
  - Made some `KtorApplicationSettings` properties public: `engine`, `port`, `host`, `watchPaths`

## [0.0.1] - 2022-06-02

Initial release of Tegral.

### Added

- `tegral-catalog`
  - Initial release
- `tegral-config`
  - Initial release
- `tegral-core`
  - Initial release
- `tegral-di-core`
  - Initial release
- `tegral-di-services`
  - Initial release
- `tegral-di-test`
  - Initial release
- `tegral-di-test-mockk`
  - Initial release
- `tegral-featureful`
  - Initial release
- `tegral-logging`
  - Initial release
- `tegral-services-api`
  - Initial release
- `tegral-services-feature`
  - Initial release
- `tegral-web-appdefaults`
  - Initial release
- `tegral-web-appdsl`
  - Initial release
- `tegral-web-config`
  - Initial release
- `tegral-web-controllers`
  - Initial release
- `tegral-web-controllers-test`
  - Initial release

[Unreleased]: https://github.com/utybo/Tegral/compare/v0.0.1..main
[0.0.1]: https://github.com/utybo/Tegral/compare/v0.0.1
