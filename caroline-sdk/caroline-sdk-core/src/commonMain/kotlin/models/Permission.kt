package cloud.caroline.core.models

import cloud.caroline.internal.carolineJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

@Serializable
public enum class Services {
    ALL,
    LOGGING,
    CRASH,
    USER,
    FUNCTIONS,
    PROJECTS,
}

@Serializable
public sealed class Permission {

    @Serializable
    public data object Global : Permission()

    @Serializable
    public data class Admin(
        val createProjects: Boolean = false,
    ) : Permission()

    @Serializable
    public data class Project(
        val projectId: String,
        val read: Boolean = false,
        val modify: Boolean = false,
        val delete: Boolean = false,
    ) : Permission()

    @Serializable
    public data class UseServices(
        val projectId: String,
        val services: Set<Services> = emptySet(),
    ) : Permission() {

        public fun canUse(service: Services): Boolean {
            return services.any { it == Services.ALL || it == service }
        }
    }

    @Serializable
    public data class Logging(
        val logKey: String,
        val projectId: String,
        val read: Boolean = false,
        val modify: Boolean = false,
        val delete: Boolean = false,
    ) : Permission()

    @Serializable
    public data class Functions(
        val functionId: String,
        val projectId: String,
        val invoke: Boolean = false,
        val read: Boolean = false,
        val modify: Boolean = false,
        val delete: Boolean = false,
    ) : Permission()

    public companion object {
        public fun valueOf(string: String): Permission? {
            return try {
                carolineJson.decodeFromString(string)
            } catch (e: SerializationException) {
                null
            }
        }
    }
}
