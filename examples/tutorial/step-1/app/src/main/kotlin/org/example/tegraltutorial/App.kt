package org.example.tegraltutorial

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.web.appdsl.tegral

fun main() {
    tegral {
        put(::HelloController)
    }
}
