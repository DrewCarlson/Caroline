plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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
        named("commonMain") {
            dependencies {
                implementation(project(":caroline-sdk:caroline-sdk-internal"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")

                implementation("io.ktor:ktor-client-core:$KTOR_VERSION")
                implementation("io.ktor:ktor-client-serialization:$KTOR_VERSION")
            }
        }

        named("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
    }
}
