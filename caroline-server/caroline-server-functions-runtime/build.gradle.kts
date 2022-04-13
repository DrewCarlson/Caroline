import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

apply(from = rootProject.file("gradle/publishing.gradle.kts"))

dependencies {
    implementation(projects.carolineServer.carolineServerInternal)

    api(projects.carolineSdk.carolineSdkCore)
    api(projects.carolineSdk.carolineSdkAdmin)
    api(projects.carolineServer.carolineServerCore)

    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.websockets)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization)

    implementation(libs.bouncyCastle)

    implementation(libs.logback)
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
