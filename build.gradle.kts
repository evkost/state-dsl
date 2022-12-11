plugins {
    kotlin("jvm") version "1.7.21" apply false
    id("com.google.devtools.ksp") version "1.7.21-1.0.8" apply false
    `java-library`
    `maven-publish`
    signing
}

java {
    withSourcesJar()
    withJavadocJar()
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "state-dsl"
            groupId = "io.github.evkost"
            version = "1.0.0-beta02"

            from(components["java"])

            pom {
                packaging = "jar"
                name.set("state-dsl")
                url.set("https://github.com/evkost/state-dsl")
                description.set(
                    "This is library that generating dsl for updating StateFlow (MutableStateFlow i mean). " +
                        "More information in repository. See: https://github.com/evkost/state-dsl"
                )

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                scm {
                    connection.set("scm:https://github.com/evkost/state-dsl.git")
                    developerConnection.set("scm:git@github.com:https:evkost/state-dsl.git")
                    url.set("https://github.com/evkost/state-dsl")
                }

                developers {
                    developer {
                        id.set("evkost")
                        name.set("Murat Kostoev")
                        email.set("mrtkostoev@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesUrl =
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = project.properties["ossrhUsername"].toString()
                password = project.properties["ossrhPassword"].toString()
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", "1.7.21"))
    }
}