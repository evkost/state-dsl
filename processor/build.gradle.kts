plugins {
    `java-library`
    `publish-plugin`
    org.jetbrains.kotlin.jvm
}

dependencies {
    implementation(project(":core"))
    implementation(project(":extension:flow"))
    implementation(libs.ksp.symbol.processing.api)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.kotlinx.coroutines.core)
}