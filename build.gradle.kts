import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

@Suppress("DSL_SCOPE_VIOLATION")
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

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    plugins.withType<NodeJsRootPlugin> {
        the<YarnRootExtension>().lockFileDirectory = rootDir.resolve("gradle/kotlin-js-store")
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
