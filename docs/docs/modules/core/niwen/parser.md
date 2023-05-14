# Parser

Tegral Niwen Parser, part of the [Niwen](index.mdx) project, is a parser framework with a primary focus on ease-of-use.

Note that you'll need to use a _lexer_ first to create tokens that Niwen Parser can understand. You can use [Tegral Niwen Lexer](lexer.md) for this, but you can also just manually create `Token` instances if you wish to use another lexer.

## Parser structure

Niwen Parser is based around the concept of parsing into some _model_ made of Kotlin classes. Then, you will _describe_ (as in, provide the _expectations_ for) these models.

This allows us to decouple several moving parts of the parser:

1. The data model (or AST) that we want to parse to
2. The description of the language we want to parse, i.e. the expectations for the various parts of our model.
3. The parser implementation itself, which is handled by Tegral Niwen parser

This decoupling is not something you'll always get with manually-written parsers, because it can get quite cumbersome to force yourself to split parts 2 and 3. Niwen takes the pain of writing all the boilerplate and lets you focus on your parser model, which is especially important considering [the main philosophy of Niwen is to allow you to prototype quickly](./index.mdx#side-note-philosophy-and-background).

Creating parsers with Niwen Parser is a two-step process, which we'll describe below.

### Data model

Anything that implements `ParserNodeDeclaration<T>`, where `T` is the type of the model node, can be used to identify a "describable" part of the model, i.e. something we can describe in our parser.

In almost all cases, and unless you're converting some external model you can't alter, you should use the following pattern of a data class (for the model) and a companion object (for the declaration):

```kotlin
data class MyModelNode(
    val someProperty: String,
    val someOtherProperty: Int
) {
    companion object : ParserNodeDeclaration<MyModelNode> by reflective()
}
```

Note that the `by reflective()` parts allows us to let Niwen Parser automatically create nodes via reflection. This is the recommended way of creating nodes, as it allows us to avoid a massive amount of boilerplate, but, if needed, you can also manually implement the `ParserNodeDeclaration.make` function. Refer to the KDoc comments for more information.

If your model contains a large amount of nodes, it may be interesting to prefix the class' name with some letter, especially because some possible class name (such as `Number` to model some number) could clash with some built-in classes from Kotlin. For example, if we were parsing some "SumLang" language, we could name the class `SNumber` instead of `Number`. This is not required, but it can be useful.

Note that, by design, your model will contain several classes that include one another. Here's an example of what we could do to model a sum:

```kotlin
data class SSum(val left: SNumber, val right: SNumber) {
    companion object : ParserNodeDeclaration<SSum> by reflective()
}

data class SNumber(val value: Int) {
    companion object : ParserNodeDeclaration<SNumber> by reflective()
}
```

### Describing the model

Taking the "sum" example from before, we also need to describe how each node of our model should be parsed. This is done when creating the actual parser, in the `niwenParser` block:

```kotlin
val parser = niwenParser {
    SSum root {
        expect(SNumber) storeIn SSum::left
        expect(Tokens.PLUS)
        expect(SNumber) storeIn SSum::right
    }

    SNumber {
        expect(Tokens.NUMBER) transform { it.toInt() } storeIn SNumber::value
    }
}
```

Each node is described using a succession of various expectations. Here, we're telling Tegral Niwen that `SSum` nodes:

- [expect](#expect-node) an `SNumber` node that should be stored in `SSum::left`
- [expect](#expect-token) a `Tokens.PLUS` token (and does not store its content anywhere)
- [expect](#expect-node) another `SNumber` node that should be stored in `SSum::right`

We're also telling Tegral Niwen that `SNumber` nodes [expect](#expect-token) a `Tokens.NUMBER` token, and that it should [transform](#transform) this token into an `Int` and store it in the `SNumber::value` property.

We need to use `transform` because the type of the value `storeIn` will store is inferred from the `expect` call. When using an `expect(SomeNode)`, the value is an instance `SomeNode`, but with tokens, it's just a `String`. We use Kotlin's `String.toInt()` function to turn this string into an integer.

:::note When transform fails

Depending on our lexer, we should be able to assume that any token of type `Tokens.NUMBER` will be a valid integer. However, if, for whatever reason, the content of a `transform { }` block fails with an exception, the parser will consider the expectation to have failed, similarly to how an `expect(Tokens.NUMBER)` would fail if the current token was to be some `Tokens.PLUS` token.

:::

Note that the order of the expectations is important, as it defines the order in which the parser will try to parse the various parts of the model.

Also note that we put `root` next to `SSum`. This tells Niwen that we want our parser to return a `SSum` node, and that we start with the expectations of `SSum`.

If, at any point, something does not hold up, the parser will stop execution and provide you an exception with basic information. You can also use a [debugging mode](#debugging) to get full information on the execution flow of your parser.

### Putting it all together

We can now call `parser.parse(tokens)` to run the parser! Here's a full example you can try at home:

```kotlin
enum class Tokens : TokenType {
    NUMBER,
    PLUS
}

val lexer = niwenLexer {
    state {
        matches("\\d+") isToken Tokens.NUMBER
        '+' isToken Tokens.PLUS
        ' '.ignore
    }
}

data class SSum(val left: SNumber, val right: SNumber) {
    companion object : ParserNodeDeclaration<SSum> by reflective()
}

data class SNumber(val value: Int) {
    companion object : ParserNodeDeclaration<SNumber> by reflective()
}

val parser = niwenParser {
    SSum root {
        expect(SNumber) storeIn SSum::left
        expect(Tokens.PLUS)
        expect(SNumber) storeIn SSum::right
    }

    SNumber {
        expect(Tokens.NUMBER) transform { it.toInt() } storeIn SNumber::value
    }
}

val input = "1 + 2"
val result = parser.parse(lexer.tokenize(input))
println(result) // SSum(left=SNumber(value=1), right=SNumber(value=2))
```

That might look like a lot of work at first and, to be honest, it is. But the main appeal of systems like this is that you'll easily be able to grow your model and your parser as your language grows, and you'll be able to do so in a very structured way.

## Node declarations

Niwen Parser provides several implementations of `ParserNodeDeclaration<T>` that you can use to declare your nodes.

### `reflective()`

The recommended way to declare nodes is to use `reflective()`. This will use Kotlin reflection to create nodes. This avoids creating boilerplate.

#### Constructor selection

The `reflective()` implementation tries (to the best of its ability) to handle situations of multiple, possibly conflicting constructors being available on the node class.

The algorithm for selecting a constructor is as follows:

- Filter down constructors to those that:
  - Have parameters that all have a name.
  - AND have parameters that
    - match the currently available stored values (i.e. what was made available through `storeIn`). These parameters must have the same name and a compatible type.
    - OR, if no such value is stored, be optional
- Then:
  - use the constructor with the most non-optional parameters (i.e. the most "precise" one)
  - OR, if no matching constructor was found, throw an exception with details on what happened.

This process is not foolproof: try to only have one constructor (with optional parameters if needed) on your model classes, if possible.

### `subtype()`

In combination with [`storeIn self()`](#storein-self), allows using sealed classes in your model.

For example, if we wanted to model a value that could either be a string or an integer, we could do it like so:

```kotlin
sealed class SValue {
    // highlight-start
    companion object : ParserNodeDeclaration<SValue> by subtype()
    // highlight-end
}

data class SString(val value: String) : SValue() {
    companion object : ParserNodeDeclaration<SString> by reflective()
}

data class SInt(val value: Int) : SValue() {
    companion object : ParserNodeDeclaration<SInt> by reflective()
}
```

## Expectations

Several expectations can be used to describe how a node should be parsed.

### Expect (token)

Expect a token of the given type to be present at this point.

```kotlin
SWord {
    expect(Tokens.PLUS)
    expect(Tokens.KEYWORD, withValue = "if")
    expect(Tokens.WORD) storeIn SWord::value
}
```

You can optionally restrict this expectation to the exact value the token must have by using the `withValue` parameter.

This expectation emits a `String` value that you can then use with `storeIn`, `transform`, etc.

### Expect (node)

Expect a node of the given type to be present at this point, and parse said node using its own description.

```kotlin
CurrentNode {
    expect(OtherNode)
    expect(OtherNode) storeIn CurrentNode::someField
}
```

Given the node declaration provided as a parameter is of type `ParserNodeDeclaration<T>`, this expectation emits a `T` value that you can then use with `storeIn`, `transform`, etc.

### Emit

An expectation that does not advance the input, but emits a value of your choice.

```kotlin
SomeNode {
    emit("hello")
    emit(42) storeIn SomeNode::someField
}
```

Emits a given value of your choice that can then be used with `storeIn`, `transform`, etc.

This is useful for advanced scenarios, such as providing fallback values when using `either`, etc.

### Either

Expects one of its branches to match, tried in the order they're defined.

```kotlin
SomeNode {
    either {
        expect(Tokens.SOMETHING) storeIn SomeNode::one
        // ...
    } or {
        // ...
    } or {
        // ...
    }
}
```

The `either` call itself does **not** emit anything, but you can use `storeIn`, `transform`, etc. inside the branches of the `either` call, and they'll be executed transparently, as if the `either` call was not there.

### Expect (EOF)

You can use `expectEof()` to indicate that, at this point, you expect the end of the input to have been reached.

This expectation does not emit anything.

```kotlin
SomeNode {
    // ...
    expectEof()
}
```

### Look ahead

Expects the provided expectation block to match at the current point, but does not consume tokens in the input outside the `lookahead` block.

```kotlin
SomeNode {
    // At index 0
    expect(Tokens.DOT)
    // At index 1
    lookahead {
        expect(Tokens.SOMETHING)
        // At index 2
        expect(Tokens.SOMETHING_ELSE)
        // At index 3
    }
    // Still at index 1
}
```

This is conceptually similar to the "look ahead" mechanism present in regular expressions.

### Optional

Create a branch that can optionally be expected:

- If the branch matches, it as if the `optional` call was not there and the content of the `optional` block is executed transparently.
- If the branch does not match, it is as if the entire `optional` block was not there.

```kotlin
SomeNode {
    expect(Tokens.DOT)
    optional {
        expect(Tokens.SOMETHING)
    }
}
```

Like [either](#either), the `optional` call itself does not emit anything, but you can use `storeIn`, `transform`, etc. inside the `optional` block, and they'll be executed transparently, as if the `optional` call was not there.

### Repeated

Repeatedly runs a provided block of expectations until it fails to match. Optionally, you can:

- Store the results of each run of the block into a list via `storeIn item`.
- Set a minimum and/or maximum amount of times the block must run for the entire `repeated` expectation to be considered as matched or not.

```kotlin
SomeNode {
    // Storing items, then storing the resulting list:
    repeated {
        expect(Tokens.SOMETHING)
        expect(OtherNode) storeIn item
        expect(Tokens.SOMETHING_ELSE)
    } storeIn SomeNode::someList

    // Same thing, but also providing a minimum/maximum amount of repetitions:
    repeated(min = 5, max = 10) {
        // ...
    }
}
```

There are two points where you can use `storeIn` and other [state callbacks](#state-callbacks):

- Inside the `repeated` block. You can only store _items_ here and do not have access to the node you're currently describing. Use `storeIn item` to store the current item.
- The output of the entire `repeated` block (next to the closing `}` of the block) is a `List<T>` where `T` is the type emitted by the `storeIn item`.

## State callbacks

_State callbacks_ are placed after an expectation (e.g. after the `expect(...)` call).

State callbacks' main usage is to do something with the value the expectation emits. Some expectations will emit value (refer to the expectation's documentation for details on what is emitted), and we manipulate, store, etc. this value using state callbacks.

State callbacks can be chained and will be executed from left to right.

### `storeIn`

This state callback allows you to store the current value into the storage that will be used to construct the node you're currently describing.

```kotlin
SomeNode {
    expect(Tokens.SOME_TOKEN) storeIn SomeNode::someField
}
```

#### `storeIn self()`

This is a special use case for `storeIn`. When dealing with sealed classes, you can use `by subtype()` on the parent class to be able to do things like this:

```kotlin
SomeExpression {
    either {
        // highlight-start
        expect(SomeNumber) storeIn self()
        // highlight-end
    } or {
        // highlight-start
        expect(SomeString) storeIn self()
        // highlight-end
    }
}

SomeCall {
    expect(SomeExpression)
}
```

#### Manually creating keys

In case your model classes do not use `reflective()` for their node declaration, you may need to use different _keys_.

Keys are used to identify items of the storage map used to create model classes. When using `reflective()`, you can directly refer to a property, and it will be turned into a key for you.

However, you may want to add a value with a key that does not actually correspond to a property of the model class. In this case, you can manually create a key to use with `storeIn` using `key<T>(name)`, where:

- `T` is the type of the value you want to store.
- `name` is the name of the key.

```kotlin
SomeNode {
    expect(Tokens.SOME_TOKEN) storeIn key<String>("someField")
}
```

Note that Kotlin type inference will sometimes be able to completely remove the `<String>` type parameter. Use this to your advantage to keep your descriptions clean!

### `transform`

Uses a lambda to turn an emitted value into another emitted value, possibly of a different type.

```kotlin
SomeNode {
    expect(Tokens.SOME_TOKEN) transform { it.toInt() } storeIn ...
}
```

## Debugging

You can use `parseWithDebugger` instead of `parse` on the parser instance to parse with a debugger. The debugger in question allows you to get a report of exactly what happened during the parsing process, including useful information like storage map, any match failure that happened at any point, etc.

The report is mostly valid YAML, albeit very strange-looking. You can open it in any editor of your choice (that supports YAML) to get syntax highlighting, etc.

```yaml
---
Root: ✅ Parsing successful
Stored:
  Parser root result (kotlin.Any?): "SSum(left=SNumber(value=1), right=SNumber(value=2))"
Expectations:
- ExpectedNode(SNumber): ✅ Node matched successfully
  Stored:
    left (SNumber): SNumber(value=1)
  Expectations:
  - expect(NUMBER): ✅ Token '1' is of correct type NUMBER
    Stored:
      value (kotlin.Int): 1
- expect(PLUS): ✅ Token '+' is of correct type PLUS
- ExpectedNode(SNumber): ✅ Node matched successfully
  Stored:
    right (SNumber): SNumber(value=2)
  Expectations:
  - expect(NUMBER): ✅ Token '2' is of correct type NUMBER
    Stored:
      value (kotlin.Int): 2
```

All built-in expectations are wired up to show you detailed output of their execution flow. Here's an example with `either`:

```yaml
---
Root: ✅ Parsing successful
Stored:
  Parser root result (kotlin.Any?): SNumber(value=3)
Expectations:
- either { 3 branch(es) }: ✅ Branch 2 matched
  Stored:
    value (kotlin.Int): 3
  Expectations:
  - Branch 0: "❌ At index 0, expected token of type NUMBER with value '1', but encountered NUMBER ('3')"
    Expectations:
    - expect(NUMBER, withValue = '1'): "❌ At index 0, expected token of type NUMBER with value '1', but encountered NUMBER ('3')"
  - Branch 1: "❌ At index 0, expected token of type NUMBER with value '2', but encountered NUMBER ('3')"
    Expectations:
    - expect(NUMBER, withValue = '2'): "❌ At index 0, expected token of type NUMBER with value '2', but encountered NUMBER ('3')"
  - Branch 2: ✅ All expectations in branch matched
    Stored:
      value (kotlin.Int): 3
    Expectations:
    - expect(NUMBER): ✅ Token '3' is of correct type NUMBER
      Stored:
        value (kotlin.Int): 3
```
