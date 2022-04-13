import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

apply(from = rootProject.file("gradle/publishing.gradle.kts"))

dependencies {
    api(project(":caroline-sdk:caroline-sdk-core"))
    api(project(":caroline-sdk:caroline-sdk-admin"))

    implementation(libs.ktor.server.permissions)

    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.client.core)

    implementation(libs.kmongo)

    testImplementation(libs.ktor.server.tests)
}

kotlin {
    explicitApi()
    sourceSets["main"].kotlin.srcDirs("src")
    sourceSets["test"].kotlin.srcDirs("test")
}

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=kotlin.RequiresOptIn")
    }
}
