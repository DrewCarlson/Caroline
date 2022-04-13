import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

apply(from = rootProject.file("gradle/publishing.gradle.kts"))

dependencies {
    api(project(":caroline-sdk:caroline-sdk-core"))
    api(project(":caroline-sdk:caroline-sdk-admin"))

    implementation("org.drewcarlson:ktor-permissions:$KTOR_PERM_VERSION")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")

    implementation("io.ktor:ktor-server-core:$KTOR_VERSION")
    implementation("io.ktor:ktor-client-core:$KTOR_VERSION")

    implementation("org.litote.kmongo:kmongo-coroutine-serialization:$KMONGO_VERSION")

    testImplementation("io.ktor:ktor-server-tests:$KTOR_VERSION")
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
        freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}
