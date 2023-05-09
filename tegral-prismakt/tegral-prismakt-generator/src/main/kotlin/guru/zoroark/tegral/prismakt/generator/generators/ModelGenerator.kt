package guru.zoroark.tegral.prismakt.generator.generators

/**
 * A generator that takes in a [context][GeneratorContext] and does *something* with it.
 *
 * Most implementations generate Kotlin classes with KotlinPoet using the information contained in the
 * [GeneratorContext].
 */
interface ModelGenerator {
    /**
     * Perform the model generation with the provided [context].
     */
    fun generateModels(context: GeneratorContext)
}
