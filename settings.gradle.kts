pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

//tasks.withType<JavaCompile>().configureEach {
//    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
//    targetCompatibility = JavaVersion.VERSION_1_8.toString()
//}

rootProject.name = "StateDsl"
include(":core")
include(":processor")
include(":example")
include("extension:flow")
