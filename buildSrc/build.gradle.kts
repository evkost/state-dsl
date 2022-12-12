plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.22.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")
}

gradlePlugin {
    plugins {
        register("publishPlugin") {
            id = "publish-plugin"
            implementationClass = "plugin.PublishPlugin"
        }
    }
}
