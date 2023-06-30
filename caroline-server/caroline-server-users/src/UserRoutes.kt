package cloud.caroline

import cloud.caroline.core.models.CreateSessionBody
import cloud.caroline.core.models.CreateSessionResponse
import cloud.caroline.core.models.CreateUserBody
import cloud.caroline.core.models.CreateUserResponse
import cloud.caroline.core.models.User
import cloud.caroline.core.models.UserCredentials
import cloud.caroline.data.UserSession
import cloud.caroline.service.CarolineUserService
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.ktor.describe
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
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
            body {
                description = "Details for the new user."
                json {
                    schema<CreateUserBody>()
                }
            }

            "createSession" queryParameter {
                description = "When true, create a session and return the ${UserSession.KEY} header."
                schema<Boolean>()
            }

            OK.value response {
                json {
                    schema<CreateUserResponse>()
                }
                UserSession.KEY header {
                    // TODO: description = "The user session string."
                }
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
                body {
                    json {
                        schema<CreateSessionBody>()
                    }
                }
                OK.value response {
                    description = "The session has been created for the user."
                    json {
                        schema<CreateSessionResponse>()
                    }
                }
            }
        }
    }
}

@KtorDsl
private infix fun Route.describeUsers(
    block: OperationDsl.() -> Unit,
) = describe {
    block()
    tags += "Users"
    security("JWT")
    security("Session")
}
