package guru.zoroark.tegral.web.apptest

import guru.zoroark.tegral.core.TegralException

/**
 * An exception that occurs during an integration test, usually because of some incorrect configuration.
 */
class TegralIntegrationTestException(message: String, cause: Throwable? = null) : TegralException(message, cause)
