package guru.zoroark.tegral.config.core

import guru.zoroark.tegral.core.TegralException

/**
 * Exception thrown when a section was not found in a [SectionedConfiguration].
 */
class UnknownSectionException(sectionName: String) : TegralException(
    "Attempted to get an unknown configuration section named '$sectionName'"
)
