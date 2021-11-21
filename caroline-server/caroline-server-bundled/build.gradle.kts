import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(project(":caroline-server:caroline-server-core"))
    implementation(project(":caroline-server:caroline-server-internal"))
    implementation(project(":caroline-server:caroline-server-users"))
    implementation(project(":caroline-server:caroline-server-projects"))
    implementation(project(":caroline-server:caroline-server-logging"))
    implementation(project(":caroline-server:caroline-server-crash"))

    implementation(project(":caroline-sdk:caroline-sdk-core"))
    implementation(project(":caroline-sdk:caroline-sdk-admin"))

    implementation("org.drewcarlson:ktor-permissions:$KTOR_PERM_VERSION")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KOTLIN_VERSION")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")

    implementation("io.ktor:ktor-server-netty:$KTOR_VERSION")
    implementation("io.ktor:ktor-metrics:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-core:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-sessions:$KTOR_VERSION")
    implementation("io.ktor:ktor-auth:$KTOR_VERSION")
    implementation("io.ktor:ktor-auth-jwt:$KTOR_VERSION")
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

    testImplementation(kotlin("test-junit"))
    testImplementation("io.ktor:ktor-server-tests:$KTOR_VERSION")
}

kotlin {
    sourceSets["main"].kotlin.srcDirs("src")
    sourceSets["test"].kotlin.srcDirs("test")
}

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-XXLanguage:+InlineClasses",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=kotlinx.coroutines.FlowPreview",
            "-Xopt-in=kotlin.time.ExperimentalTime",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
}
