plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    val pluginsDef = listOf(
        libs.plugins.kotlin,
        libs.plugins.kotlinSerialization,
        libs.plugins.dokkatoo,
        libs.plugins.detekt,
        libs.plugins.versions,
        libs.plugins.gradleTestLogger,
        libs.plugins.kotlinBcv,
        libs.plugins.nodeGradle,
        libs.plugins.licenser,
        libs.plugins.shadow
    )

    for (pluginDef in pluginsDef) {
        val actualLib = pluginDef.get()
        implementation(
            group = actualLib.getPluginId(),
            name = actualLib.getPluginId() + ".gradle.plugin",
            version = actualLib.getVersion().toString()
        )
    }

    // Usual hack to get access to 'libs' https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
