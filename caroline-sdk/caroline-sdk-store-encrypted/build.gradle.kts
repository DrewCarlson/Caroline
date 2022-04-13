plugins {
    kotlin("multiplatform")
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
                implementation(project(":caroline-sdk:caroline-sdk-internal"))
                api(project(":caroline-sdk:caroline-sdk-store"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")
            }
        }
    }
}
