import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
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
    implementation(projects.carolineServer.carolineServerCore)
    implementation(projects.carolineServer.carolineServerInternal)
    implementation(projects.carolineServer.carolineServerAdmin)
    implementation(projects.carolineServer.carolineServerAnalytics)
    implementation(projects.carolineServer.carolineServerUsers)
    implementation(projects.carolineServer.carolineServerProjects)
    implementation(projects.carolineServer.carolineServerLogging)
    implementation(projects.carolineServer.carolineServerCrash)
    implementation(projects.carolineServer.carolineServerFunctions)

    implementation(projects.carolineSdk.carolineSdkCore)
    implementation(projects.carolineSdk.carolineSdkAdmin)

    implementation(libs.ktor.server.permissions)

    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.metrics)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authJwt)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.cachingHeaders)
    implementation(libs.ktor.server.conditionalHeaders)
    implementation(libs.ktor.server.forwardedHeader)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.autoHeadResponse)
    implementation(libs.ktor.server.websockets)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization)

    implementation(libs.bouncyCastle)

    implementation(libs.logback)

    implementation(libs.mongo.driver)
    implementation(libs.mongo.bson)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.ktor.server.tests)
}

kotlin {
    sourceSets["main"].kotlin.srcDirs("src")
    sourceSets["test"].kotlin.srcDirs("test")
}

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")
