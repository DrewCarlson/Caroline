plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

apply(from = rootProject.file("gradle/publishing.gradle.kts"))

kotlin {
    jvm()
    js(IR) {
        nodejs()
        browser()
    }

    sourceSets {
        all {
            explicitApi()
        }

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:$DATETIME_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")

                implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
            }
        }
    }
}
