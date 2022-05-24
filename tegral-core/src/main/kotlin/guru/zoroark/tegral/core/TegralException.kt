package guru.zoroark.tegral.core

@Suppress("UnnecessaryAbstractClass") // Abstract because not meant be used as-is
abstract class TegralException(message: String, cause: Throwable? = null) : Exception(message, cause)
