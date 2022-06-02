# Tegral Catalog

Tegral Catalog is a [shared Gradle version catalog](https://docs.gradle.org/current/userguide/platforms.html#sub:central-declaration-of-dependencies) that you can import in your own projects. It makes it easier to add Tegral dependencies to your project and ensures that all Tegral dependencies version numbers are synced within your project.

```groovy
implementation tegralLibs.config.core
implementation tegralLibs.di.core

testImplementation tegralLibs.di.test
```

Tegral Catalog is completely optional. You can add Tegral libraries just like regular Maven dependencies using the usual `groupId:artifactId:version` syntax.

## Adding Tegral Catalog to your project

Tegral Catalog is a settings plugin, meaning that you will need to add it in your `settings.gradle` file.

```groovy title="settings.gradle"
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        tegralLibs {
            from("guru.zoroark.tegral:tegral-catalog:VERSION")
        }
    }
}
```

Refer to [the version catalog documentation](https://docs.gradle.org/current/userguide/platforms.html#sec:importing-published-catalog) for more information.

## Dependencies and bundles

The catalog is generated dynamically from all of the published components in the [Tegral repository](https://github.com/utybo/Tegral).

Additionally, the catalog contains a few [bundles](https://docs.gradle.org/current/userguide/platforms.html#sec:dependency-bundles) mainly intended for applications that use the full Tegral platform and not just individual libraries. The bundles are as follows:

### `web`

Full notation: `tegralLibs.bundles.web`

This bundle includes all of the required components for building Tegral Web applications (i.e. Tegral Web AppDSL, Tegral Web AppDefaults and all of their dependencies, such as Tegral DI or Tegral Config).

```groovy title=build.gradle
dependencies {
    // highlight-start
    implementation tegralLibs.bundles.web
    // highlight-end
}
```

### `web-test`

Full notation: `tegralLibs.bundles.web.test`

This bundle is the equivalent of the `web` component but for use in tests. It includes components recommended for testing Tegral applications, such as `tegral-web-controllers-test` or `tegral-di-test`.

```groovy title=build.gradle
dependencies {
    // highlight-start
    testImplementation tegralLibs.bundles.web.test
    // highlight-end
}
```
