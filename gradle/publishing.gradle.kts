apply(plugin = "maven-publish")

System.getenv("GITHUB_REF")?.let { ref ->
    if (ref.startsWith("refs/tags/")) {
        version = ref.substringAfterLast("refs/tags/")
    }
}

val mavenUrl: String by extra
val mavenSnapshotUrl: String by extra

configure<PublishingExtension> {
    components.findByName("java")?.let { javaComponent ->
        publications {
            register<MavenPublication>("mavenJava") {
                from(javaComponent)
            }
        }
    }
    repositories {
        maven {
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri(mavenSnapshotUrl)
            } else {
                uri(mavenUrl)
            }
            credentials {
                username = System.getenv("BINTRAY_USER")
                password = System.getenv("BINTRAY_API_KEY")
            }
        }
    }
}
