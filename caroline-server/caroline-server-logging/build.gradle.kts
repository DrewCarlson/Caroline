plugins {
    id("caroline-server-lib")
}

dependencies {
    implementation(projects.carolineServer.carolineServerInternal)

    api(projects.carolineSdk.carolineSdkCore)
    api(projects.carolineSdk.carolineSdkAdmin)
    api(projects.carolineSdk.carolineSdkLogging)
    api(projects.carolineServer.carolineServerCore)

    implementation(libs.ktor.server.permissions)

    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)

    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.sessions)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.websockets)

    implementation(ktorLibs.client.core)
    implementation(ktorLibs.client.okhttp)
    implementation(ktorLibs.client.logging)
    implementation(ktorLibs.client.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)

    implementation(libs.bouncyCastle)

    implementation(libs.logback)

    implementation(libs.mongo.driver)
    implementation(libs.mongo.bson)
}
