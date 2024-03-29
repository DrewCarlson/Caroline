import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    explicitApi()
    sourceSets["main"].kotlin.srcDirs("src")
    sourceSets["test"].kotlin.srcDirs("test")
}

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findBundle("tegral-ktor").get())

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation(libs.findLibrary("ktor-server-tests").get())
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.majorVersion
    }
}
