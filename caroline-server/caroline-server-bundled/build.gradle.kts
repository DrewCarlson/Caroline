import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.shadowjar)
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

    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.metrics)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.sessions)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.compression)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.cachingHeaders)
    implementation(ktorLibs.server.conditionalHeaders)
    implementation(ktorLibs.server.forwardedHeader)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.autoHeadResponse)
    implementation(ktorLibs.server.websockets)

    implementation(ktorLibs.client.core)
    implementation(ktorLibs.client.okhttp)
    implementation(ktorLibs.client.logging)
    implementation(ktorLibs.client.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)

    implementation(libs.bouncyCastle)

    implementation(libs.logback)

    implementation(libs.mongo.driver)
    implementation(libs.mongo.bson)

    implementation(ktorLibs.server.routingOpenapi)
    implementation(ktorLibs.server.openapi)
    implementation(ktorLibs.server.swagger)

    testImplementation(kotlin("test-junit"))
    testImplementation(ktorLibs.server.testHost)
}

kotlin {
    sourceSets["main"].kotlin.srcDirs("src")
    sourceSets["test"].kotlin.srcDirs("test")
}

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")
