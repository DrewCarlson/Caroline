import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

application {
    applicationName = "caroline-server"
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("caroline-server.jar")
    archiveBaseName.set("caroline-server")
    archiveClassifier.set("caroline-server")
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass.get()))
    }
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
    implementation("io.ktor:ktor-server-metrics:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-core:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-sessions:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-auth:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-auth-jwt:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-compression:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-cors:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-caching-headers:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-conditional-headers:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-forwarded-header:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-call-logging:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-partial-content:$KTOR_VERSION")
    implementation("io.ktor:ktor-server-auto-head-response:$KTOR_VERSION")
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
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=kotlin.RequiresOptIn")
    }
}
