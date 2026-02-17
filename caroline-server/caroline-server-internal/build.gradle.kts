plugins {
    id("caroline-server-lib")
}

dependencies {
    api(projects.carolineSdk.carolineSdkCore)
    api(projects.carolineSdk.carolineSdkAdmin)

    implementation(libs.ktor.server.permissions)

    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)

    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.client.core)
}
