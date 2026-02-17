import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
}

kotlin {
    jvmToolchain(21)
    explicitApi()
    compilerOptions {
        optIn.add("io.ktor.utils.io.ExperimentalKtorApi")
        jvmTarget.set(JvmTarget.JVM_21)
    }
    sourceSets["main"].kotlin.srcDirs("src")
    sourceSets["test"].kotlin.srcDirs("test")
}

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val ktorLibs = extensions.getByType<VersionCatalogsExtension>().named("ktorLibs")

application {
    // Unused in library modules, but required for the ktor gradle plugin
    mainClass.set("io.ktor.server.netty.EngineMain")
}

ktor {
    openApi {
        enabled.set(true)
        codeInferenceEnabled.set(false)
    }
}

dependencies {
    implementation(ktorLibs.findLibrary("server-routingOpenapi").get())

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation(ktorLibs.findLibrary("server-testHost").get())
}
