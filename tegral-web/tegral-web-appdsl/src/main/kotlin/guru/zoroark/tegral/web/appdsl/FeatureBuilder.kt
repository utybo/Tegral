package guru.zoroark.tegral.web.appdsl

import guru.zoroark.tegral.config.core.RootConfig
import guru.zoroark.tegral.featureful.Feature

class FeatureContext(
    val configuration: RootConfig
)

class FeatureBuilder<T>(
    val feature: Feature<T>,
    val configBuilder: T.(FeatureContext) -> Unit
)
