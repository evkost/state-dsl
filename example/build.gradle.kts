plugins {
    alias(libs.plugins.ksp)
    `java-library`
    org.jetbrains.kotlin.jvm
}

sourceSets {
    main {
        java {
            srcDir("${buildDir.absolutePath}/generated/ksp/")
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":extension:flow"))
    implementation(project(":processor"))
    ksp(project(":processor"))
}