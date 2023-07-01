import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

fun KotlinMultiplatformExtension.configureFramework(block: Framework.() -> Unit) {
    iosArm64 { binaries { framework(configure = block) } }
    iosSimulatorArm64 { binaries { framework(configure = block) } }
    iosX64 { binaries { framework(configure = block) } }
}


fun KotlinMultiplatformExtension.iosAll() {
    iosAll { }
}

fun KotlinMultiplatformExtension.iosAll(configure: Action<KotlinNativeTarget>) {
    iosArm64(configure)
    iosSimulatorArm64 { configure.execute(this) }
    iosX64 { configure.execute(this) }

    if (sourceSets.any { it.name == "iosMain" }) {
        // Skip if we already have ios source sets defined
        return
    }

    val iosMain = sourceSets.create("iosMain") {
        dependsOn(sourceSets.getByName("commonMain"))
    }
    val iosTest = sourceSets.create("iosTest") {
        dependsOn(sourceSets.getByName("commonTest"))
    }

    sourceSets.getByName("iosArm64Main") { dependsOn(iosMain) }
    sourceSets.getByName("iosArm64Test") { dependsOn(iosTest) }
    sourceSets.getByName("iosSimulatorArm64Main") { dependsOn(iosMain) }
    sourceSets.getByName("iosSimulatorArm64Test") { dependsOn(iosTest) }
    sourceSets.getByName("iosX64Main") { dependsOn(iosMain) }
    sourceSets.getByName("iosX64Test") { dependsOn(iosTest) }
}

