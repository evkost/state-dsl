import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "1.7.21" apply false
    id("com.google.devtools.ksp") version "1.7.21-1.0.8" apply false
    id("com.vanniktech.maven.publish") version "0.22.0"
    `java-library`

}

group = "io.github.evkost.state-dsl"
version = "1.0.0-beta03"

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)

    signAllPublications()

    pom {
        packaging = "jar"
        name.set("state-dsl")
        url.set("https://github.com/evkost/state-dsl")
        inceptionYear.set("2022")
        description.set(
            "This is library that generating dsl for updating StateFlow (MutableStateFlow i mean). " +
                    "More information in repository. See: https://github.com/evkost/state-dsl"
        )

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", "1.7.21"))
    }
}