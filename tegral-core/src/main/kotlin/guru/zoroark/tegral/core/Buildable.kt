package guru.zoroark.tegral.core

/**
 * Represents a builder that can be turned into an object of type `T`.
 */
fun interface Buildable<T> {
    /**
     * Build the current object into a [T] object.
     */
    fun build(): T

    companion object {
        /**
         * Creates a [Buildable] object that will always return the given [result].
         */
        fun <T> of(result: T): Buildable<T> = Buildable { result }
    }
}
