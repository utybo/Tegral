package guru.zoroark.tegral.web.appdefaults

import guru.zoroark.tegral.config.core.RootConfig
import guru.zoroark.tegral.config.core.TegralConfig

/**
 * The default configuration object for `tegral.toml` files.
 *
 * In its default form, this configuration file only contains the `[tegral]` section. See the Configuration section of
 * the Tegral Web documentation for more information on how to configure your applications.
 */
data class TegralConfigurationContainer(override val tegral: TegralConfig) : RootConfig
