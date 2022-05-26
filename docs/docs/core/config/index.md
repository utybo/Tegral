# Tegral Config

Tegral Config is an extension library that adds extra features on top of [Hoplite](https://github.com/sksamuel/hoplite).

Tegral Config aims to supplement the "data class" model of defining configuration files by implementing [Hoplite decoders](https://github.com/sksamuel/hoplite#decoders) for custom classes. You can use such decoders in your own projects, although Tegral Config does not provide the necessary SPI "auto-registration" of decoders: you will need to add them manually to the `ConfigLoaderBuilder` object via `addDecoder(...)`.
