package cloud.caroline

import cloud.caroline.core.models.CreateSessionBody
import cloud.caroline.core.models.CreateSessionResponse
import cloud.caroline.core.models.CreateUserBody
import cloud.caroline.core.models.CreateUserResponse
import cloud.caroline.core.models.User
import cloud.caroline.core.models.UserCredentials
import cloud.caroline.data.UserSession
import cloud.caroline.service.CarolineUserService
import guru.zoroark.koa.dsl.DescriptionBuilder
import guru.zoroark.koa.dsl.schema
import guru.zoroark.koa.ktor.describe
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import io.swagger.v3.oas.models.headers.Header
import org.litote.kmongo.coroutine.CoroutineDatabase

internal fun Route.addUserRoutes(mongodb: CoroutineDatabase) {
    val users = mongodb.getCollection<User>()
    val credentialsDb = mongodb.getCollection<UserCredentials>()
    val userService = CarolineUserService(users, credentialsDb)
    route("/user") {
        post {
            val body = call.receiveNullable<CreateUserBody>()
                ?: return@post call.respond(UnprocessableEntity)
            val createSession = call.parameters["createSession"]?.toBoolean() ?: true

            val response = userService.createUser(body, setOf())
            if (createSession && response is CreateUserResponse.Success) {
                call.sessions.getOrSet {
                    UserSession(userId = response.user.id, response.permissions)
                }
            }
            call.respond(response)
        } describeUsers {
            summary = "Create a new user."
            security("JWT")
            security("Session")
            ContentType.Application.Json requestBody {
                description = "Details for the new user."
                schema<CreateUserBody>()
            }

            "createSession" queryParameter {
                description = "When true, create a session and return the ${UserSession.KEY} header."
                schema<Boolean>()
            }

            OK response ContentType.Application.Json {
                schema<CreateUserResponse>()
                headers[UserSession.KEY] = Header().description("The user session string.")
            }
        }

        route("/session") {
            post {
                val body = call.receiveNullable<CreateSessionBody>()
                    ?: return@post call.respond(UnprocessableEntity)

                val response = userService.createSession(body)
                if (response is CreateSessionResponse.Success) {
                    call.sessions.set(UserSession(response.user.id, response.permissions))
                }
                call.respond(response)
            } describe {
                summary = "Create a new user session."
                tags += "Users"
                ContentType.Application.Json requestBody {
                    schema<CreateSessionBody>()
                }
                OK response ContentType.Application.Json {
                    description = "The session has been created for the user."
                    schema<CreateSessionResponse>()
                }
            }
        }
    }
}

@KtorDsl
private infix fun Route.describeUsers(
    block: DescriptionBuilder.() -> Unit,
) = describe {
    block()
    tags += "Users"
    security("JWT")
    security("Session")
}
