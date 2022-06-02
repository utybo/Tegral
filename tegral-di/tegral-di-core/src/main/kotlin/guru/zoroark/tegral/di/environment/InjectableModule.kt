package guru.zoroark.tegral.di.environment

/**
 * A module that can be injected in environments. At their core, injectable modules are just lists of
 * [declarations][Declaration] that get copied over when adding this module to environments.
 *
 * @property name The name of this module. It is only used for debugging purposes. May be empty.
 */
class InjectableModule(val name: String, defs: Collection<Declaration<*>>) {
    /**
     * The declarations contained in this module.
     */
    val declarations: List<Declaration<*>> = defs.toList() // Copy to a list
}
