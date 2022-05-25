package guru.zoroark.tegral.core

/**
 * Root exception class for all exceptions emitted by Tegral.
 *
 * Note that this class should not be instantiated directly and should be subclassed into more detailed and appropriate
 * exceptions.
 */
@Suppress("UnnecessaryAbstractClass") // Abstract because not meant be used as-is
abstract class TegralException(message: String, cause: Throwable? = null) : Exception(message, cause)
