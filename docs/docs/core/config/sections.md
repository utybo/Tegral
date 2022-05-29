# Sectioned configuration

A common pattern in configuration frameworks is to have some sort of "container" that can contain arbitrary sections.

Let's take the `[tegral]` section for example. In Tegral Web applications, each feature can optionally contribute a section. For example, Tegral Web Config provides a `[tegral.web]` section, which contains configuration for the web application, such as the port and host. Other features can also contribute sections under `[tegral.*]`. How can this be implemented?

## Creating sectioned configurations

There are multiple parts at play here:

- The section configuration decoder, which is implemented by Tegral Config. This decoder is instantiated by your code with the sections you wish to support.
- The sectioned configuration class, which is a subclass of `SectionedConfiguration`. For example, Tegral applications have a `TegralConfig` class. It is recommended that you subclass it yourself if you want to have your own sectioned configuration.
- Configuration sections, which are the actual bits that can be embedded within your sectioned configuration.
- The configuration class, which simply embeds the sectioned configuration(s) class(es) as regular data class properties.

Let's say that we want to create a `FooConfig` sectioned configuration.

### Defining the class

This class will usually just be empty, except for the fact that it subclasses `SectionedConfiguration`. Internally, Tegral Config takes advantage of Kotlin's type system to differentiate between multiple `SectionedConfiguration`s that live within the same configuration hierarchy.

In our example, our sectioned configuration class will look like this:

```kotlin
class FooConfig(sections: ConfigurationSections) : SectionedConfiguration(sections)
```

### Defining the sections

Since the entire point of sectioned configurations is to be able to contain arbitrary configuration sections, let's define a few as an example. Note that these configuration sections are not specially linked anywhere, the actual binding between sections and sectioned configs happens when we instantiate the decoder.

A section can be defined using the following pattern, where:

- The actual section content is defined in a data class (this is required for Hoplite to automatically decode this class).
- The section data class gets a companion object that subclasses `ConfigurationSection`. This companion object additionally defines the name of the section (e.g. the name used in the configuration file) as well as whether it is required or not, and if not, the default value.

```kotlin
// An example of a mandatory section
data class SectionOne(
    val one: String,
    // ...
) {
    companion object : ConfigurationSection<SectionOne>("one", SectionOptionality.Required, SectionOne::class)
}

// And an example of an optional section
data class SectionTwo(
    val two: String,
    // ...
) {
    companion object : ConfigurationSection<SectionTwo>(
        "two",
        SectionOptionality.Optional(SectionTwo("Two (default)!")),
        SectionTwo::class
    )
}
```

The data classes themselves are not special in any way and are just the same plain data classes you would use with Hoplite.

### Defining the configuration class

Now that we have a type for our sectioned configuration, we can add it as a regular property within our configuration.

```kotlin
data class Config(
    val foo: FooConfig,
    val something: String = "Something!"
)
```

### Setting up the decoder

If you try to just pass `Config` to Hoplite, you will get a nasty error telling you that Hoplite does not know how to decode it. We'll give Hoplite an instance of our decoder, which we will set up with a few sections:

```kotlin
val loader = ConfigLoaderBuilder.default()
    // highlight-start
    .addDecoder(
        SectionedConfigurationDecoder(
            FooConfig::class,
            ::FooConfig,
            listOf(SectionOne, SectionTwo)
        )
    )
    // highlight-end
    .build()
```

Now, if we try to parse the following configuration file (here in TOML, but you can use any format supported by Hoplite):

```toml title="./example.toml"
something = "Hello World!"

[foo.one]
one = "One!"

[foo.two]
two = "Two!"
```

We get:

```kotlin
val loader = ConfigLoaderBuilder.default()
    .addDecoder(
        SectionedConfigurationDecoder(
            FooConfig::class,
            ::FooConfig,
            listOf(SectionOne, SectionTwo)
        )
    )
    .addPathSource(Path.of("example.toml"))
    .build()

val config = loader.loadConfigOrThrow<Config>()
println(config)
// Config(foo=FooConfig(one=SectionOne(one=One!), two=SectionTwo(two=Two!)), something=Hello World!)
```

## Consuming sectioned configurations

You can get sectioned configuration objects in a few ways:

- either by parsing a configuration file as described [in the section above](#creating-sectioned-configurations)
- via your environment providing you with, e.g. when using Tegral Web AppDSL <!-- TODO AppDSL link -->
- by instantiating a `SectionedConfiguration` object yourself (but where's the fun in that?)

Once you have such an object, you can access the sections either by using the `sections` property, or by using the `get` operator directly:

```kotlin
data class SectionOne(val one: String) {
    companion object : ConfigurationSection<SectionOne>(/* ... */)
}

data class SectionTwo(val two: String) {
    companion object : ConfigurationSection<SectionTwo>(/* ... */)
}

class ExampleConfig(sections: ConfigurationSections) : SectionedConfiguration(sections)

data class Config(val example: ExampleConfig)

val config: Config = /* ... */

// highlight-start
val one = config.example[SectionOne].one
println(one) // One!
val two = config.example[SectionTwo].two
println(two) // Two!
// highlight-end
```

## Limitations

Sectioned configurations have the following limitations:

### Multiple decoders

Sectioned configuration decoders cannot have different section sets registered for the same sectioned configuration class within the same configuration hierarchy.

Let's take the following example:

```kotlin
class ExampleConfig(sections: ConfigurationSections) : SectionedConfiguration(sections)

data class A(val exampleA: ExampleConfig)

data class B(val exampleB: ExampleConfig)

data class Config(
    val a: A,
    val b: B
)
```

In this example, both `Config.a.exampleA` and `Config.b.exampleB` will be decoded using the same decoder, meaning that they will be using the same set of sections (i.e. the same section classes).

If you want multiple decoders with different section sets, you will need to define a new sectioned configuration class for each "section set" and add a decoder for each sectioned configuration class. <!-- TODO not super clear but I'm not sure of how to explain it better -->
