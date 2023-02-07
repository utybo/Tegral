package guru.zoroark.tegral.di.extensions.fundef

@ExperimentalFundef
annotation class TegralDiFundef()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS)
@RequiresOptIn()
annotation class ExperimentalFundef
