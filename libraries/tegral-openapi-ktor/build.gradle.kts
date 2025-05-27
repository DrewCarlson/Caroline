plugins {
    id("caroline-server-lib")
}

dependencies {
    api(project(":libraries:tegral-openapi-dsl"))

    api(libs.ktor.server.core)

    //testImplementation(libs.ktor.server.test)
}
