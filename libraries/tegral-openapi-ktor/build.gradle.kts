plugins {
    id("caroline-server-lib")
}

dependencies {
    api(project(":libraries:tegral-openapi-dsl"))

    api(ktorLibs.server.core)

    //testImplementation(ktorLibs.server.test)
}
