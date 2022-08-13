---
sidebar_position: 2
---

# Introduction to Dependency Injection

This page is a general introduction to what dependency injection aims to solve. Feel free to skip ahead to the next page if you already know what you're in for.

## A busy chef

Let's imagine that we are making a cooking server where a Chef wants to cook a cake. In order to make a cake, the Chef will need a cupboard, which in turn has access to an egg provider, a flour provider and some yeast, as well as kitchen tools such as an oven and a cake mold.

We can create the following classes to model our problem:

```kotlin
class Chef {
    fun makeSomeCake() {
        // ???
    }
}

class Cupboard {
    fun get(ingredientKind: IngredientKind): String {
        // ???
    }
}

enum class IngredientKind { Egg, Flour, Yeast }

class EggProvider {
    fun getEgg(): String {
        return "an egg"
    }
}

class FlourProvider {
    fun getFlour(): String {
        return "some flour"
    }
}

class YeastProvider {
    fun getYeast(): String {
        return "some yeast"
    }
}

class Oven {
    fun useOven(cookWhat: String, temperatureCelsius: Int, timeMinutes: Int) {
        println("Cooking $cookWhat at $temperatureCelsius°C for $time minutes")
    }
}

class CakeMold {
    fun useCakeMold(vararg ingredients: String): String {
        return "Cake mold with " + ingredients.joinToString()
    }
}
```

The problem now is to "link" these classes together: the chef will need access to the cupboard, the oven and the cake mold, and the cupboard will need access to the egg, flour and yeast providers.

This is where injection comes in. Thanks to Tegral DI, we can request the cupboard, the oven and the cake mold from the environment, and the chef will be able to access them without having to know about the objects directly (i.e. without passing them through the constructor).

Let's modify our first two classes to use dependency injection:

```kotlin
class Chef(scope: InjectionScope) {
    private val cupboard: Cupboard by scope()
    private val oven: Oven by scope()
    private val cakeMold: CakeMold by scope()

    fun makeSomeCake() {
        val egg = cupboard.get(IngredientKind.Egg)
        val flour = cupboard.get(IngredientKind.Flour)
        val yeast = cupboard.get(IngredientKind.Yeast)

        val filledCakeMold = cakeMold.useCakeMold(egg, flour, yeast)
        oven.useOven(filledCakeMold, 180, 25)
        println("Cake has been baked successfully!")
    }
}

class Cupboard(scope: InjectionScope) {
    private val eggProvider: EggProvider by scope()
    private val flourProvider: FlourProvider by scope()
    private val yeastProvider: YeastProvider by scope()

    fun get(ingredient: IngredientKind): String {
        return when(ingredient) {
            Ingredient.Egg -> eggProvider.getEgg()
            Ingredient.Flour -> flourProvider.getFlour()
            Ingredient.Yeast -> yeastProvider.getYeast()
        }
    }
}
```

Here, we added two things:

* A `scope: InjectionScope` parameter to our classes' constructor. This scope provides information on how to retrieve the objects that the classes need upon injection (i.e. upon using `by scope()`).
* Our dependencies, expressed as `by scope()` [delegated properties](https://kotlinlang.org/docs/delegated-properties.html), which are now retrieved from the environment.

Now that our classes know how to request things, it's time to make the environment in which all these objects will live. In Tegral DI, this is done by using the `tegralDi` function.

```kotlin
val environment = tegralDi {
    put { Chef(scope) }
    put { Cupboard(scope) }

    put { EggProvider() }
    put { FlourProvider() }
    put { YeastProvider() }

    put { Oven() }
    put { CakeMold() }
}
```

This creates an environment with all of the objects we provide within the `tegralDi` block. Each `put` call adds an object to the environment.

* Classes with constructors that do not require a scope (in our case, everything but the chef and the cubpoard) can be created with a regular constructor call.
* Classes that require a scope (in our case, the chef and the cubpoard) can be created using the `scope` variable that is automatically available within the block of a `put` call.

## Going further

Our basic example has already shown you 80% of the job when using Tegral DI, but there are a few things we can do to make our life easier.

### Simpler `put` calls

You may notice that our `put` calls are somewhat repetitive -- creating a new lambda every time is not paraticularily fun.

Tegral DI provides a shortcut for cases where the constructor of the component we'd like to inject does not take any argument *or* only takes an injection scope. We can just reference the constructor of a class `Foo` by using `::Foo`:

```kotlin
val environment = tegralDi {
    put(::Chef)
    put(::Cupboard)

    put(::EggProvider)
    put(::FlourProvider)
    put(::YeastProvider)

    put(::Oven)
    put(::CakeMold)
}
```

### Modularizing

Tegral DI provides a way to modularize our code. We can create a module that contains all the objects we need to create a specific "facet" of our overall environment. The semantics of what a module is up to you: in Tegral DI's eyes, a module is just a bunch of component definitions.

In our example, we can split our environment into two modules: one for the chef and his kitchen tools and one for the cupboard and its content.

```kotlin
val chefModule = tegralDiModule {
    put(::Chef)
    put(::Oven)
    put(::CakeMold)
}

val cupboardModule = tegralDiModule {
    put(::Cupboard)
    put(::EggProvider)
    put(::FlourProvider)
    put(::YeastProvider)
}

val environment = tegralDi {
    put(chefModule)
    put(cupboardModule)
}
```

### Testing support

Tegral DI provides tools to aid the creation of test in environments: see our [testing documentation](./testing/index.md) for more information.
