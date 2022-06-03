package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.test.environment.ExtensibleEnvironmentBaseTest
import guru.zoroark.tegral.di.test.environment.NotExtensibleEnvironmentBaseTest
import kotlin.test.Test

class TestUnsafeMutableEnvironment : ExtensibleEnvironmentBaseTest(::UnsafeMutableEnvironment) {
    @Test
    fun `Base test`() {
        runTests()
    }

    @Test
    fun `Base test (on actual injection env)`() {
        val baseTest = object : NotExtensibleEnvironmentBaseTest({ context ->
            UnsafeMutableEnvironment.MutableEnvironment(null, context)
        }) {}
        baseTest.runTests()
    }
}
