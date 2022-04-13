package cloud.caroline.internal

import io.ktor.server.application.*
import io.ktor.server.routing.*

public fun Route.carolineProperty(key: String, default: String? = null): String =
    application.carolineProperty(key, default)

public fun Route.carolinePropertyInt(key: String, default: Int? = null): Int =
    application.carolineProperty(key, default.toString()).toInt()

public fun Application.carolineProperty(key: String, default: String? = null): String {
    val configValue = environment.config.propertyOrNull("caroline.$key")
    return checkNotNull(configValue?.getString() ?: default) {
        "Application config `caroline.$key` did not exist and default was null."
    }
}
