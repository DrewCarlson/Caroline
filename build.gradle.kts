import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.shadowjar) apply false
    alias(libs.plugins.binaryCompat) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
}

yarn.lockFileDirectory = rootDir.resolve("gradle/kotlin-js-store")

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/**.kt")
            // licenseHeaderFile(rootDir.resolve("licenseHeader.txt"))
            ktlint(libs.versions.ktlint.get())
                .userData(mapOf("disabled_rules" to "no-wildcard-imports,no-unused-imports"))
        }
    }
}
