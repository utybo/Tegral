package guru.zoroark.tegral.config.core

import guru.zoroark.tegral.core.TegralException

class UnknownSectionException(sectionName: String) : TegralException(
    "Attempted to get an unknown configuration section named '$sectionName'"
)
