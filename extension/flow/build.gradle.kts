plugins {
    `java-library`
    //`publish-plugin`
    org.jetbrains.kotlin.jvm
}

dependencies {
    api(project(":core"))
    api(libs.kotlinx.coroutines.core)
}