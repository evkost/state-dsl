

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21" apply false
    id("com.google.devtools.ksp") version "1.7.21-1.0.8" apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", "1.7.21"))
    }
}