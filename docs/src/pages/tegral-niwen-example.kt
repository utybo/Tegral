enum class MyTokenTypes : LixyTokenType {
    DOT, WORD, WHITESPACE
}

val lexer = niwenLexer {
    state {
        "." isToken DOT
        anyOf(" ", "\n", "\t") isToken WHITESPACE
        matches("[A-Za-z]+") isToken WORD
    }
}
