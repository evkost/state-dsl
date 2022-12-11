plugins {
    id("com.google.devtools.ksp") version "1.7.21-1.0.8"
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

sourceSets {
    main {
        java {
            srcDir("${buildDir.absolutePath}/generated/ksp/")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":core"))
    implementation(project(":processor"))
    ksp(project(":processor"))
}