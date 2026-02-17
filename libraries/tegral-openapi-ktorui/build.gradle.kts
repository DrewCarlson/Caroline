plugins {
    id("caroline-server-lib")
}

dependencies {
    implementation(libs.swaggerUi)
    implementation(ktorLibs.server.core)

    //testImplementation(ktorLibs.server.test)
}
