package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.TegralDiException

/**
 * Error thrown when a feature that does not exist in controlled test environments (e.g. environments internally used by
 * tegralDiCheck checks) is accessed. If you did not initiate the missing feature yourself:
 *
 * - Ensure that you are using only safe injections (see the `safeInjection` check)
 * - Otherwise, consider reporting it, as it may be a bug from Tegral DI's checks.
 */
class NotAvailableInTestEnvironmentException(message: String) : TegralDiException(message)
