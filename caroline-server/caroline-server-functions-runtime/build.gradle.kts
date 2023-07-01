plugins {
    id("caroline-server-lib")
}

dependencies {
    implementation(projects.carolineServer.carolineServerInternal)

    api(projects.carolineSdk.carolineSdkCore)
    api(projects.carolineSdk.carolineSdkAdmin)
    api(projects.carolineServer.carolineServerCore)

    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.websockets)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization)

    implementation(libs.bouncyCastle)

    implementation(libs.logback)
}
