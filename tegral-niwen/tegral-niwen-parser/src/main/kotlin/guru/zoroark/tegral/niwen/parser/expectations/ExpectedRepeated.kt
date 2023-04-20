package guru.zoroark.tegral.niwen.parser.expectations

import guru.zoroark.tegral.niwen.parser.ExpectationResult
import guru.zoroark.tegral.niwen.parser.NiwenParserException
import guru.zoroark.tegral.niwen.parser.ParsingContext
import guru.zoroark.tegral.niwen.parser.dsl.ExpectationReceiver
import kotlin.reflect.full.isSubtypeOf

sealed interface RepeatedItemReceiver<R>

class ExpectedRepeated<T, R>(
    private val repeatableExpectations: List<Expectation<RepeatedItemReceiver<R>, *>>,
    stateCallback: StateCallback<T, List<R>, *>? = null
) : Expectation<T, List<R>>(stateCallback) {
    override fun matches(context: ParsingContext, index: Int): ExpectationResult<T> {
        var currIndex = index
        val valueAcc = mutableListOf<R>()
        var matchCount = 0
        val stopReason: String
        while (true) {
            when (val next = repeatableExpectations.applyExpectations(context, currIndex)) {
                is ExpectationResult.DidNotMatch -> {
                    stopReason = next.message
                    break
                }

                is ExpectationResult.Success -> {
                    currIndex = next.nextIndex
                    matchCount++
                    next.stored.entries.firstOrNull()?.let { valueAcc += it.value as R }
                }
            }
        }
        return ExpectationResult.Success(stateCallback.createStoreMap(valueAcc), currIndex, "After $matchCount successful iterations, stopped at index $currIndex because: $stopReason")
    }
}