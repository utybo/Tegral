import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
    id("com.github.node-gradle.node")
    id("base")
}

val web by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}


val docsInputFiles = listOf(
    "babel.config.js",
    "docusaurus.config.ts",
    "sidebars.js"
)

val docsInputDirs = listOf(
    "blog",
    "docs",
    "src",
    "static"
)

tasks.register("cleanNodeModules") {
    doLast {
        delete("node_modules")
    }
}

tasks.register<PnpmTask>("docusaurusStart") {
    args = listOf("start")

    inputs.file("package.json")
    inputs.file("pnpm-lock.yaml")

    for (input in docsInputFiles) {
        inputs.file(input)
    }
    for (input in docsInputDirs) {
        inputs.files(input)
    }
}

val docusaurusBuild = tasks.register<PnpmTask>("docusaurusBuild") {
    args = listOf(
        "build"
    )

    inputs.file("package.json")
        .withPropertyName("packageJson")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file("pnpm-lock.yaml")
        .withPropertyName("pnpmLock")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    for (input in docsInputFiles) {
        inputs.file(input)
            .withPropertyName("docsFile-$input")
            .withPathSensitivity(PathSensitivity.RELATIVE)
    }
    for (input in docsInputDirs) {
        inputs.dir(input)
            .withPropertyName("docsDir-$input")
            .withPathSensitivity(PathSensitivity.RELATIVE)
    }

    outputs.dir(layout.buildDirectory.dir("docusaurus"))
        .withPropertyName("result")
}

artifacts {
    add("web", layout.buildDirectory.dir("docusaurus")) {
        builtBy(docusaurusBuild)
    }
}
