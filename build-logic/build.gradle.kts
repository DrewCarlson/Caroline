plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation("io.ktor.plugin:io.ktor.plugin.gradle.plugin:${libs.versions.ktor.asProvider().get()}")
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.kotlin:kotlin-serialization:${libs.versions.serialization.get()}")
    implementation("org.gradle.kotlin:gradle-kotlin-dsl-conventions:0.9.0")
}
