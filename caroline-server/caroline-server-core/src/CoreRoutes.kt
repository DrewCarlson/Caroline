package cloud.caroline

import cloud.caroline.admin.api.ApiKeyCredentials
import cloud.caroline.admin.api.ProjectDetails
import cloud.caroline.core.models.*
import cloud.caroline.data.ProjectUserSession
import cloud.caroline.service.CarolineProjectService
import cloud.caroline.service.CarolineUserService
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import kotlinx.coroutines.flow.firstOrNull

internal fun Route.addCoreRoutes(mongodb: MongoDatabase) {
    val projectsDb = mongodb.getCollection<Project>("project")
    val projectDetailsDb = mongodb.getCollection<ProjectDetails>("project-details")
    val apiKeyCredsDb = mongodb.getCollection<ApiKeyCredentials>("api-key-credentials")
    val usersDb = mongodb.getCollection<User>("user")
    val userCredentialsDb = mongodb.getCollection<UserCredentials>("user-credentials")
    val userService = CarolineUserService(usersDb, userCredentialsDb)
    val projectService = CarolineProjectService(projectsDb, projectDetailsDb, apiKeyCredsDb)
    route("/core") {
        authenticate(PROVIDER_API_JWT, PROVIDER_USER_SESSION, optional = true) {
            post("/token") {
                val apiKey = call.request.header(X_CAROLINE_API_KEY)
                val jwtApiKey = call.principal<ProjectUserSession>()?.apiKey
                val credentials = (apiKey ?: jwtApiKey)
                    ?.takeUnless(String::isNullOrBlank)
                    ?.let { apiKeyCredsDb.find(Filters.eq("_id", it)) }
                    ?.firstOrNull()
                    ?: return@post call.respond(Unauthorized)

                call.respond(JwtManager.createToken(credentials.projectId))
            }.describe {
                summary = "Create an API token for the project."
                tag("Core")
                security {
                    requirement("JWT")
                }
                parameters {
                    header(X_CAROLINE_API_KEY) {
                        description = "The API key to create a token for."
                        required = true
                        schema = jsonSchema<String>()
                    }
                }
                responses {
                    response(OK.value) {
                        description =  "The new session token."
                    }
                    response(Unauthorized.value) {
                        description = "The API Key is missing or invalid."
                    }
                }
            }
        }

        addSetupRoutes(userService, projectService)
    }
}
