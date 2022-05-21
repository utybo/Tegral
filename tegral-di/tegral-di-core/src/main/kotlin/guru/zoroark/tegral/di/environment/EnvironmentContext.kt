package guru.zoroark.tegral.di.environment

/**
 * Data objects that contain all of the information passed to [InjectionEnvironment] constructors to let them initialize
 * their data.
 *
 * This is the main link between the DSL (which outputs objects of this class) and the actual environments (which
 * consume objects of this class).
 *
 * @property declarations The declarations contained in this context
 */
class EnvironmentContext(val declarations: Declarations)
