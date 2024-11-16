plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

publishing {
    repositories {
        if (project.hasProperty("publishSnapshot")) {
            if (project.version.toString().endsWith("-SNAPSHOT")) {
                maven {
                    name = "mavenCentralSnapshots"
                    url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    credentials(PasswordCredentials::class.java)
                }
            } else {
                logger.warn("-PpublishSnapshot is present, but no -SNAPSHOT suffix: skipping snapshot deployment")
            }
        } else if (project.hasProperty("publishRelease")) {
            if (project.version.toString().endsWith("-SNAPSHOT")) {
                error("Cannot use -PpublishRelease when version ends in -SNAPSHOT")
            }
            maven {
                name = "mavenCentralRelease"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials(PasswordCredentials::class.java)
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            pom {
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "utybo"
                        name = "Matthieu S"
                        email = "utybodev@gmail.com"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/utybo/Tegral.git"
                    developerConnection = "scm:git:ssh://github.com:utybo/Tegral.git"
                    url = "https://github.com/utybo/Tegral"
                }

                afterEvaluate {
                    for (requiredExtProp in listOf("humanName", "description", "url")) {
                        if (!project.extensions.extraProperties.properties.containsKey(requiredExtProp)) {
                            error("Missing project.ext.$requiredExtProp")
                        }
                    }
                    this@pom.name = project.extra["humanName"]!!.toString()
                    this@pom.description = project.extra["description"]!!.toString()
                    this@pom.url = project.extra["url"]!!.toString()
                }
            }
        }
    }
}

if (project.properties.containsKey("sign")) {
    signing {
        if (!project.properties.containsKey("signLocally")) {
            val signingKey = findProperty("signingKey")!!.toString()
            val signingPassword = findProperty("signingPassword")!!.toString()
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
        sign(publishing.publications["maven"])
    }
}
