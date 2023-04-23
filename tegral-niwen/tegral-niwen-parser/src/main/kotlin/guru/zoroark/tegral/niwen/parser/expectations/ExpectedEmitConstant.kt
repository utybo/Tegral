package guru.zoroark.tegral.niwen.parser.expectations

import guru.zoroark.tegral.niwen.parser.ExpectationResult
import guru.zoroark.tegral.niwen.parser.ParsingContext

class ExpectedEmitConstant<T, R>(
    val value: R,
    stateCallback: StateCallback<T, R, *>? = null
) : Expectation<T, R>(stateCallback) {
    override fun matches(context: ParsingContext, index: Int): ExpectationResult<T> {
        return ExpectationResult.Success(
            stateCallback.createStoreMap(value),
            index,
            index to index,
            "Emitted value $value"
        )
    }

    override val title: String = "emit($value)"
}
