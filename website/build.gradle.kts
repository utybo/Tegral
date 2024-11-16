plugins {
}

repositories {
    mavenCentral()
    maven { url = uri("https://gitlab.com/api/v4/projects/29365238/packages/maven") }
}

val servine by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}
val docusaurus by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}
val dokkaHtml by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}


dependencies {
    servine("guru.zoroark.servine:servine:0.0.2-SNAPSHOT")
    docusaurus(project(path = ":docs", configuration = "web"))
    dokkaHtml(project(path = ":dokka", configuration = "dokkaHtml"))

}

val assembleFiles = tasks.register<Copy>("assembleFiles") {
    into(layout.buildDirectory.dir("output"))

    from(docusaurus)
    from(dokkaHtml) {
        into("dokka")
    }
    from("_redirects")
}

val serve = tasks.register<JavaExec>("serve") {
    mainClass = "guru.zoroark.servine.app.AppKt"
    classpath = servine

    dependsOn(assembleFiles)

    val params = listOf(layout.buildDirectory.dir("output").get().toString())
    args(params)
}
