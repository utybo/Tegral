package guru.zoroark.tegral.config.core

/**
 * Root configuration for Tegral appliactions.
 *
 * Tegral application configurations, even custom ones, msut contain a way to add `[tegral.*]` blocks as most
 * first-party features use this section to configure their own settings. This interface enforces this pattern even if
 * you use custom data classes for your configuration.
 */
interface RootConfig {
    /**
     * The [TegralConfig] instance for this configuration.
     */
    val tegral: TegralConfig
}
