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
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }

    sourceSets {
        all {
            explicitApi()
        }
        val commonMain by getting {
            dependencies {
                api(project(":caroline-sdk:caroline-sdk-core"))
                implementation(project(":caroline-sdk:caroline-sdk-internal"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")
            }
        }
    }
}
