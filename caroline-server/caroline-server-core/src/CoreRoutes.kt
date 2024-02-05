package cloud.caroline

import cloud.caroline.admin.api.ApiKeyCredentials
import cloud.caroline.admin.api.ProjectDetails
import cloud.caroline.core.models.*
import cloud.caroline.data.ProjectUserSession
import cloud.caroline.service.CarolineProjectService
import cloud.caroline.service.CarolineUserService
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.ktor.describe
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineDatabase

internal fun Route.addCoreRoutes(mongodb: CoroutineDatabase) {
    val projectsDb = mongodb.getCollection<Project>()
    val projectDetailsDb = mongodb.getCollection<ProjectDetails>()
    val apiKeyCredsDb = mongodb.getCollection<ApiKeyCredentials>()
    val usersDb = mongodb.getCollection<User>()
    val userCredentialsDb = mongodb.getCollection<UserCredentials>()
    val userService = CarolineUserService(usersDb, userCredentialsDb)
    val projectService = CarolineProjectService(projectsDb, projectDetailsDb, apiKeyCredsDb)
    route("/core") {
        authenticate(PROVIDER_API_JWT, PROVIDER_USER_SESSION, optional = true) {
            post("/token") {
                val apiKey = call.request.header(X_CAROLINE_API_KEY)
                val jwtApiKey = call.principal<ProjectUserSession>()?.apiKey
                val credentials = (apiKey ?: jwtApiKey)
                    ?.takeUnless(String::isNullOrBlank)
                    ?.let { apiKeyCredsDb.findOneById(it) }
                    ?: return@post call.respond(Unauthorized)

                call.respond(JwtManager.createToken(credentials.apiKey))
            } describeCore {
                summary = "Create an API token for the project."
                X_CAROLINE_API_KEY headerParameter {
                    description = "The API key to create a token for."
                    required = true
                    schema<String>()
                }
                OK.value response {
                    description = "The new session token."
                }
                Unauthorized.value response {
                    description = "The API Key is missing or invalid."
                }
            }
        }

        val requiresSetup = runBlocking { projectService.getProjectsCount() == 0L }
        if (requiresSetup) {
            addSetupRoutes(userService, projectService)
        }
    }
}

@KtorDsl
private infix fun Route.describeCore(
    block: OperationDsl.() -> Unit,
) = describe {
    block()
    tags += "Core"
    security("JWT")
}
