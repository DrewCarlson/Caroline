plugins {
    kotlin("multiplatform") version KOTLIN_VERSION apply false
    kotlin("jvm") version KOTLIN_VERSION apply false
    kotlin("plugin.serialization") version KOTLIN_VERSION apply false
    id("org.jetbrains.dokka") version DOKKA_VERSION
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://kotlin.bintray.com/ktor")
        maven(url = "https://kotlin.bintray.com/kotlinx/")
    }
}


tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask> {
    if (!name.contains("html", ignoreCase = true)) return@withType

    val docs = buildDir.resolve("dokka/htmlMultiModule")
    outputDirectory.set(docs)
    doLast {
        docs.resolve("-modules.html").renameTo(docs.resolve("index.html"))
    }
}
