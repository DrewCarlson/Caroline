import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    //id("com.android.library")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

/*android {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
    namespace = "cloud.caroline.${project.name.replace("-", "")}"
    compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()
    defaultConfig {
        minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}*/

kotlin {
    jvm()
    js(IR) {
        nodejs()
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    //iosAll()
    /*android {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_11.majorVersion
        }
    }*/
    sourceSets {
        all {
            explicitApi()
        }
        named("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        named("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
