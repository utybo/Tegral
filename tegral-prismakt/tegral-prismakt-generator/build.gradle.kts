plugins {
    id("tegral.kotlin-published-library-conventions")
    id("application")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":tegral-utils:tegral-utils-logtools"))
    implementation(project(":tegral-di:tegral-di-core"))
    implementation(project(":tegral-niwen:tegral-niwen-parser"))
    implementation(project(":tegral-niwen:tegral-niwen-lexer"))
    implementation(libs.logback)
    implementation(libs.jackson.kotlin)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.javatime)
    implementation(libs.kotlinPoet)

    testImplementation(libs.jimfs)
}

application {
    mainClass = "guru.zoroark.tegral.prismakt.generator.MainKt"
}

extra["humanName"] = "Tegral PrismaKT Generator"
extra["description"] = "A Prisma generator for writing JetBrains Exposed tables & entities"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/prismakt"

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "guru.zoroark.tegral.prismakt.generator.MainKt"
    }
}
