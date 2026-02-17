plugins {
    id("caroline-sdk-lib")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                api(libs.datetime)
                implementation(libs.coroutines.core)
                implementation(libs.serialization.json)

                implementation(ktorLibs.client.core)
            }
        }
    }
}
