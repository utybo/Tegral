package org.example.stage1.integration

import guru.zoroark.tegral.web.appdsl.integration.TegralWebIntegrationTest

class TestEverything : TegralWebIntegrationTest() {
    override fun IntegrationTestDsl.setup() {
        put(::Controller)
        put(::Service)
    }

    @Test
    fun `Test hello world`() = test {
        client.get("/")
    }
}
