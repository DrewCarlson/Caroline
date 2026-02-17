plugins {
    id("caroline-sdk-lib")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(projects.carolineSdk.carolineSdkInternal)
                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)

                implementation(ktorLibs.client.core)
                implementation(ktorLibs.client.contentNegotiation)
                implementation(ktorLibs.serialization.kotlinx.json)
            }
        }
    }
}
