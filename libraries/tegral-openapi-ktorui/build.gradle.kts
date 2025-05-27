plugins {
    id("caroline-server-lib")
}

dependencies {
    implementation(libs.swaggerUi)
    implementation(libs.ktor.server.core)

    //testImplementation(libs.ktor.server.test)
}
