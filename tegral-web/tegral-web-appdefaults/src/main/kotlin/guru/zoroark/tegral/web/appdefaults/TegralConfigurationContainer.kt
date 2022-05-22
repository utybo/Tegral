package guru.zoroark.tegral.web.appdefaults

import guru.zoroark.tegral.config.core.RootConfiguration
import guru.zoroark.tegral.config.core.SectionedConfiguration

typealias TegralConfig = SectionedConfiguration

data class TegralConfigurationContainer(override val tegral: TegralConfig) : RootConfiguration
