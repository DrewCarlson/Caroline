plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.kotlin:kotlin-serialization:${libs.versions.serialization.get()}")
    implementation("org.jetbrains.kotlinx:binary-compatibility-validator:${libs.versions.binary.compat.get()}")
    implementation("org.gradle.kotlin:gradle-kotlin-dsl-conventions:0.8.0")
}
