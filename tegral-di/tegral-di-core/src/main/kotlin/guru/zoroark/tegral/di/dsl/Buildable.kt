package guru.zoroark.tegral.di.dsl

/**
 * Represents a builder that can be turned into an object of type `T`.
 */
interface Buildable<T> {
    /**
     * Build the current object into a [T] object.
     */
    fun build(): T
}
