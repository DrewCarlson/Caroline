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
                implementation(projects.carolineSdk.carolineSdkInternal)
                api(projects.carolineSdk.carolineSdkCore)

                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)

                implementation(libs.ktor.client.core)
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

        named("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
