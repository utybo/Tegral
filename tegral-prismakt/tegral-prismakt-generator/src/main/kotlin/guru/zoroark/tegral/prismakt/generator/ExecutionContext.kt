package guru.zoroark.tegral.prismakt.generator

/**
 * Context for the execution of the entire generation process
 *
 * @property enableParsingDebug If true, enable complex debugging output, e.g. the `NiwenPrism` parser debug info.
 */
data class ExecutionContext(val enableParsingDebug: Boolean)
