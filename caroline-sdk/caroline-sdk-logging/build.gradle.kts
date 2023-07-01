plugins {
    id("caroline-sdk-lib")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(projects.carolineSdk.carolineSdkInternal)
                api(projects.carolineSdk.carolineSdkCore)

                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)

                implementation(libs.ktor.client.core)
            }
        }
    }
}
