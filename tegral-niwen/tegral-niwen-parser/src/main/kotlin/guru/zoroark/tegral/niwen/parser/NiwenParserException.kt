package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.core.TegralException

/**
 * Exception type for anything thrown by Niwen Parser.
 *
 * Note that individual expectations should not throw these exceptions when
 * they do not match: instead they should return a
 * [ExpectationResult.DidNotMatch] object. A `NiwenParserException` being thrown
 * indicates that something is very wrong, for example that the parser is
 * not configured properly.
 */
class NiwenParserException(message: String, cause: Throwable? = null) :
    TegralException(message, cause)