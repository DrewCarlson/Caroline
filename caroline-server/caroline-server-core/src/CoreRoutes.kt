package cloud.caroline

import cloud.caroline.admin.api.ApiKeyCredentials
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
