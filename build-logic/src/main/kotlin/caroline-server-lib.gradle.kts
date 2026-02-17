import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(21)
    explicitApi()
    sourceSets["main"].kotlin.srcDirs("src")
    sourceSets["test"].kotlin.srcDirs("test")
}

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val ktorLibs = extensions.getByType<VersionCatalogsExtension>().named("ktorLibs")

dependencies {
    if (!name.startsWith("tegral")) {
        implementation(project(":libraries:tegral-openapi-dsl"))
        implementation(project(":libraries:tegral-openapi-ktor"))
        implementation(project(":libraries:tegral-openapi-ktorui"))
    }

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation(ktorLibs.findLibrary("server-testHost").get())
}


tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
