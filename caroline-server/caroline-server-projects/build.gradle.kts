import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

apply(from = rootProject.file("gradle/publishing.gradle.kts"))

dependencies {
    implementation(project(":caroline-server:caroline-server-internal"))

    api(project(":caroline-sdk:caroline-sdk-core"))
    api(project(":caroline-sdk:caroline-sdk-admin"))
    api(project(":caroline-server:caroline-server-core"))

    implementation("org.drewcarlson:ktor-permissions:$KTOR_PERM_VERSION")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")

    implementation("io.ktor:ktor-server-core:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-sessions:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-auth:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-auth-jwt:$KTOR_VERSION")
    implementation("io.ktor:ktor-serialization:$KTOR_VERSION")
    implementation("io.ktor:ktor-websockets:$KTOR_VERSION")

    implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
    implementation("io.ktor:ktor-client-okhttp:$KTOR_VERSION")
    implementation("io.ktor:ktor-client-logging:$KTOR_VERSION")
    implementation("io.ktor:ktor-client-json:$KTOR_VERSION")
    implementation("io.ktor:ktor-client-serialization-jvm:$KTOR_VERSION")

    implementation("org.bouncycastle:bcprov-jdk15on:$BOUNCY_CASTLE_VERSION")

    implementation("ch.qos.logback:logback-classic:$LOGBACK_VERSION")

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
