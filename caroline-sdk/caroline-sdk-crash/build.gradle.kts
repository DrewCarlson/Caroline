plugins {
    kotlin("multiplatform")
}

apply(from = rootProject.file("gradle/docs.gradle.kts"))
apply(from = rootProject.file("gradle/publishing.gradle.kts"))

kotlin {
    jvm()
    js(BOTH) {
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")
            }
        }
    }
}
