package guru.zoroark.tegral.featureful

import guru.zoroark.tegral.config.core.RootConfig

/**
 * Features that require special hooking
 */
interface LifecycleHookedFeature : Feature {
    fun onConfigurationLoaded(configuration: RootConfig) {}
}

