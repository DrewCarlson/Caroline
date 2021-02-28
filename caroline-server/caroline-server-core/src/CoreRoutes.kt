package tools.caroline

import tools.caroline.admin.api.ApiKeyCredentials
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.request.header
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.withTimeout
import org.litote.kmongo.coroutine.CoroutineDatabase

internal fun Route.addCoreRoutes(mongodb: CoroutineDatabase) {
    val apiKeyCredsDb = mongodb.getCollection<ApiKeyCredentials>()
    route("/core") {
        authenticate(
            PROVIDER_ADMIN_SESSION,
            PROVIDER_API_JWT,
            optional = true,
        ) {
            post("/token") {
                val apiKey = call.request.header("Authorization")
                if (apiKey.isNullOrBlank()) {
                    return@post call.respond(BadRequest)
                }
                val credentials = apiKeyCredsDb.findOneById(apiKey)
                    ?: return@post call.respond(NotFound)

                call.respond(JwtManager.createToken(credentials.apiKey, credentials.permissions))
            }
        }
    }
}
