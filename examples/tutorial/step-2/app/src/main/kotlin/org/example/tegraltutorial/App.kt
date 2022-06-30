package org.example.tegraltutorial

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.web.appdsl.tegral

val appModule = tegralDiModule {
    put(::HelloController)
    put(::HelloService)
}

fun main() {
    tegral {
        put(appModule)
    }
}
