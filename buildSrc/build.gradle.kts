plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.publish.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("publishPlugin") {
            id = libs.plugins.publish.get().pluginId
            implementationClass = "plugin.PublishPlugin"
        }
    }
}
