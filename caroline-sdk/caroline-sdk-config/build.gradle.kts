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
            }
        }
    }
}
