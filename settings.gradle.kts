plugins {
    id("com.gradle.enterprise") version "3.12.2"
}

val publicProjects = listOf(
        ":tegral-catalog",
        ":tegral-config:tegral-config-core",
        ":tegral-core",
        ":tegral-di:tegral-di-core",
        ":tegral-di:tegral-di-services",
        ":tegral-di:tegral-di-test",
        ":tegral-di:tegral-di-test-mockk",
        ":tegral-featureful",
        ":tegral-logging",
        ":tegral-niwen:tegral-niwen-lexer",
        ":tegral-niwen:tegral-niwen-parser",
        ":tegral-openapi:tegral-openapi-cli",
        ":tegral-openapi:tegral-openapi-dsl",
        ":tegral-openapi:tegral-openapi-feature",
        ":tegral-openapi:tegral-openapi-ktor",
        ":tegral-openapi:tegral-openapi-ktor-resources",
        ":tegral-openapi:tegral-openapi-ktorui",
        ":tegral-openapi:tegral-openapi-scriptdef",
        ":tegral-openapi:tegral-openapi-scripthost",
        ":tegral-prismakt:tegral-prismakt-generator",
        ":tegral-services:tegral-services-api",
        ":tegral-services:tegral-services-feature",
        ":tegral-utils:tegral-utils-logtools",
        ":tegral-web:tegral-web-appdefaults",
        ":tegral-web:tegral-web-appdsl",
        ":tegral-web:tegral-web-apptest",
        ":tegral-web:tegral-web-config",
        ":tegral-web:tegral-web-controllers",
        ":tegral-web:tegral-web-controllers-test",
        ":tegral-web:tegral-web-greeter",
)

val testProjects = publicProjects - ":tegral-catalog" + listOf(
        ":tegral-prismakt:tegral-prismakt-generator-tests:mysql-types",
        ":tegral-prismakt:tegral-prismakt-generator-tests:pgsql-types",
        ":tegral-prismakt:tegral-prismakt-generator-tests:simple-dao",
        ":tegral-prismakt:tegral-prismakt-generator-tests:simple-sql",
        ":tegral-prismakt:tegral-prismakt-generator-tests-support",
        ":e2e-tests:fundef-modules",
        ":e2e-tests:run-with-java-11",
        ":e2e-tests:run-with-java-17",
        ":e2e-tests:run-without-config"
)

gradle.extra["publicProjects"] = publicProjects
gradle.extra["testProjects"] = testProjects

for (project in publicProjects) {
    include(project)
}
for (project in testProjects) {
    include(project)
}


include("examples:simple-json-api")

include("code-coverage")
include("docs")
include("dokka")
include("website")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
