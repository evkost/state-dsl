plugins {
    id("java-library")
    id("publish-plugin")
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.21-1.0.8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.21")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation("com.squareup:kotlinpoet:1.12.0")
}