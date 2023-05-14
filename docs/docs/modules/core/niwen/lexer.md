# Lexer

Tegral Niwen Lexer, part of the [Niwen](index.mdx) project, is a lexer framework with a primary focus on ease-of-use.

All public functions in Tegral Niwen are documented using KDoc: you should be able to see this documentation directly in your IDE.

## Lexer structure

The main entry point for Niwen Lexer is the `niwenLexer` function, within which you can create *states* (identified by their *state label*), within which you can define *matchers* (which can output *tokens* and/or perform *actions*).

```kotlin
val lexer = niwenLexer { // <- Entrypoint
    default state { // <- State
        '.' isToken Tokens.DOT thenState States.IN_STRING // <- Matcher
//       ^  ------------------ --------------------------
// Recognizer        |                       |
//                Action             Additional action
    }

    inString state {
        matches("[^\"]+") isToken Tokens.STRING_CONTENT
        '.' isToken Tokens.QUOTE thenState default
    }
}
```

If your lexer only contains one state, you can write it like this instead:

```kotlin
val lexer = niwenLexer {
    state {
        // ...
    }
}
```

## Execution

Running the lexer can be done using the `tokenize` function on the `lexer` object returned by the `niwenLexer` function.

When executing the lexer, Niwen will start in the `default` state, then try each defined matcher in the order they were written in the `state` block.

- Once a matcher successfully matches, the lexer will execute the matcher's action, which can either be to *ignore* the match (via `.ignore`) or *emit* a token (via `isToken`). *Additional actions* are also performed at this point, such as switching to a different state (via `thenState`).
- If a matcher fails to match, the lexer will try the next matcher. If there are no matchers left in the current state, the lexing process will fail with an exception.

Note that this means that precedence within a lexer state is defined by the order in which matchers are defined.

Once Niwen is done tokenizing (i.e. the entire input has been processed), it will return the list of tokens.

## Token types

Produced tokens are identified via a *token type*, which can be any `TokenType` instance.

There are two recommended ways of creating token types:

### `tokenType` function

For small projects, you can use `tokenType("myToken")` to create a token type with the provided name. The name is only used for debugging purposes. You can then use store these types in variables and use them as such:

```kotlin
val tDot = tokenType("dot")
val tGreeting = tokenType("greeting")

val lexer = niwenLexer {
    state {
        '.' isToken tDot
        "hello" isToken tGreeting
    }
}
```

### Enum classes

Enum classes are recommended for larger lexers with more than about 3 token types. You can create an enum class that implements `TokenType` like so:

```kotlin
enum class Tokens : TokenType {
    DOT,
    COMMA,
    GREETING,
    // ...
}
```

And use them in a lexer like so:

```kotlin
val lexer = niwenLexer {
    state {
        '.' isToken Tokens.DOT
        ',' isToken Tokens.COMMA
        "hello" isToken Tokens.GREETING
    }
}
```

## Recognizers

Recognizers are used on the left hand-side of a matcher (i.e. the part before `isToken` or `.ignore`). Their role is to recognize a sequence of characters and, if found, trigger the remainder of the matcher.

There are two kinds of recognizers:

- **Regular recognizers** (R), which are just instances of the `TokenRecognizer` interface. These are the actual implementations of the matchers and can be used directly in your lexer, but are usually created using special functions.
- **Pseudo-recognizers** (PR), which are not actual instances of the `TokenRecognizer` interface, but are just plain Kotlin objects that Tegral Niwen will internally convert to proper recognizers.

Here are all of the available, built-in recognizers:

### Strings (PR)

When using a string, the input will be match against the exact provided string. This matching is case-sensitive.

```kotlin
val tHello = tokenType()
val tGoodbye = tokenType()
val lexer = tegralNiwen {
    state {
        "Hello" isToken tHello
        "Goodbye" isToken tGoodbye
        " ".ignore
    }
}
val tokens = lexer.tokenize("Hello Goodbye Hello")
// tokens = [Hello{tHello},Goodbye{tGoodbye},Hello{tHello}]
```

### Characters (PR)

You can also use characters in the same way as [strings](#strings-pr). This matching is case-sensitive.

```kotlin
val tA = tokenType()
val tB = tokenType()
val lexer = niwenLexer {
    state {
        'a' isToken tA
        'b' isToken tB
    }
}
val tokens = lexer.tokenize("abba")
// tokens = [a{tA},b{tB},b{tB},a{tA}]
```

### Character ranges (PR)

Character ranges (i.e. Kotlin's `CharRange`, which can be created with `a..z`) can be used to match a single character within the provided range. This matching is case-sensitive.

```kotlin
val tac = tokenType()
val tdf = tokenType()
val lexer = niwenLexer {
    state {
        'a'..'c' isToken tac
        'd'..'f' isToken tdf
        ('A'..'Z').ignore
    }
}
val tokens = lexer.tokenize("afAXebc")
// tokens = [a{tac},f{tdf},e{tdf},b{tac},c{tac}]
```

### `anyOf` (R)

`anyOf` recognizes any of the provided strings, in a similar way to the [`String` pseudo-recognizer](#strings-pr).

The `anyOf` function takes in a vararg of strings:

```kotlin
anyOf("some string", "hi", "hello")
```

Here's a full example:

```kotlin
val thello = tokenType()
val tbonjour = tokenType()
val lexer = niwenLexer {
    state {
        anyOf("Hello", "Hi") isToken thello
        anyOf("Bonjour", "Salut") isToken tbonjour
        " ".ignore
    }
}
val tokens = lexer.tokenize("Hello Bonjour Hi")
// tokens = [Hello{thello},Bonjour{tbonjour},Hi{thello}]
```

:::tip Using lists with `anyOf`

If you need to use a `List` instead of a vararg, you can do so as follows:

```kotlin
val myStrings = listOf("Hello", "Hey", "Hi").toTypedArray()

// Then, wherever you want to create the recognizer
anyOf(*myStrings)
```

:::

When `anyOf` only contains `String`, a special `StringSetTokenRecognizer` recognizer is created which has better.

### Regex with `matches` (R)

You can use `matches` to recognize a sequence of characters using a regex pattern.

Use `matches("my[Regex]")` to do so. The pattern will be matched starting at the *current* position in the input.

```kotlin
val tNumber = tokenType()
val tWord = tokenType()
val lexer = niwenLexer {
    state {
        matches("\\d+") isToken tNumber
        matches("\\w+") isToken tWord
        " ".ignore
    }
}
val tokens = lexer.tokenize("Hello 42 World 09")
// tokens = [Hello{tWord},42{tNumber},World{tWord},09{tNumber}]
```

#### Specificities

Internally, `matches` uses Java's `Pattern` class (with `Pattern.compile`), but a few additional configurations are applied for you to make working with regexes in the lexer easier:

- Look-behind (`(?<=foobar)` and `(?<!foobar)`) will correctly look *before* the current position (`useTransparentBounds(true)`)

- Anchors (`^` and `$`) match the start and end of the input string (`useAnchoringBounds(false)`)

- Capturing groups are not used for anything at the moment. Please [open an issue](https://github.com/utybo/Tegral/issues) if you need to use groups in some form or another.

### Repeated recognizers (R)

`.repeated` allows you to repeat a recognizer as many times as possible. For example, while `'a'..'z'` will only match a single character, `'a'..'z'.repeated` will match as many characters as possible within the provided range.

`.repeated` can be used on any recognizer. You can also specify a minimum and/or maximum amount of repetitions using the `repeated(min, max)` function. Both `min` and `max` are optional parameters and are set to `null` by default to indicate "no minimum/maximum".

```kotlin
val ta = tokenType()
val tb1 = tokenType()
val tb2 = tokenType()
val tc = tokenType()
val lexer = lixy {
    state {
        "a".repeated isToken ta
        "b".repeated(max = 3) isToken tb1
        "b".repeated(4, 6) isToken tb2
        "c".repeated(2) isToken tc
    }
}
val tokens = lexer.tokenize("abbaaaacccbbbb")
// tokens = [a{ta},bb{tb1},aaaa{ta},ccc{tc},bbbb{tb2}]
```
