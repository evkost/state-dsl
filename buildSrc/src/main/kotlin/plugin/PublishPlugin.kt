package plugin

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class PublishPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.vanniktech.maven.publish")
        }

        group = "io.github.evkost"
        version = "1.0.0-beta03"

        project.extensions.getByType<MavenPublishBaseExtension>().apply {
            publishToMavenCentral(SonatypeHost.S01)

            signAllPublications()

            pom {
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

        Unit
    }
}