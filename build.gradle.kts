plugins {
    kotlin("jvm") apply false
    id("com.google.devtools.ksp") version "1.7.21-1.0.8" apply false
    `java-library`
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}