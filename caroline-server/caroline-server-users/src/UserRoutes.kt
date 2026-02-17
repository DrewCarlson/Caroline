package cloud.caroline

import cloud.caroline.core.models.CreateSessionBody
import cloud.caroline.core.models.CreateSessionResponse
import cloud.caroline.core.models.CreateUserBody
import cloud.caroline.core.models.CreateUserResponse
import cloud.caroline.core.models.User
import cloud.caroline.core.models.UserCredentials
import cloud.caroline.data.UserSession
import cloud.caroline.service.CarolineUserService
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.openapi.jsonSchema
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import io.ktor.server.sessions.*

internal fun Route.addUserRoutes(mongodb: MongoDatabase) {
    val users = mongodb.getCollection<User>("user")
    val credentialsDb = mongodb.getCollection<UserCredentials>("user-credentials")
    val userService = CarolineUserService(users, credentialsDb)
    route("/user") {
        post {
            val body = call.receiveNullable<CreateUserBody>()
                ?: return@post call.respond(UnprocessableEntity)
            val createSession = call.parameters["createSession"]?.toBoolean() ?: true

            when (val response = userService.createUser(body, setOf())) {
                is CreateUserResponse.Success -> {
                    if (createSession) {
                        call.sessions.getOrSet {
                            UserSession(userId = response.user.id, response.permissions)
                        }
                    }
                    call.respond(OK, response)
                }
                is CreateUserResponse.Failed -> {
                    call.respond(BadRequest, response)
                }
            }
        }.describe {
            summary = "Create a new user."
            tag("Users")
            security {
                requirement("JWT")
                requirement("Session")
            }
            parameters {
                query("createSession") {
                    description = "When true, create a session and return the ${UserSession.KEY} header."
                    schema = jsonSchema<Boolean>()
                }
            }
            requestBody {
                schema = jsonSchema<CreateUserBody>()
                description = "Details for the new user."
                required = true
            }
            responses {
                response(OK.value) {
                    description = "The created user."
                    schema = jsonSchema<CreateUserResponse.Success>()
                }
                response(BadRequest.value) {
                    description = "The user could not be created."
                    schema = jsonSchema<CreateUserResponse.Failed>()
                }
                response(UnprocessableEntity.value) {
                    description = "The body was malformed or missing data."
                }
            }
        }

        route("/session") {
            post {
                val body = call.receiveNullable<CreateSessionBody>()
                    ?: return@post call.respond(UnprocessableEntity)

                when (val response = userService.createSession(body)) {
                    is CreateSessionResponse.Success -> {
                        call.sessions.set(UserSession(response.user.id, response.permissions))
                        call.respond(OK, response)
                    }
                    is CreateSessionResponse.Failed -> {
                        call.respond(Unauthorized, response.errors)
                    }
                }
            }.describe {
                summary = "Create a new user session."
                tag("Users")
                requestBody {
                    schema = jsonSchema<CreateSessionBody>()
                    required = true
                }
                responses {
                    response(OK.value) {
                        description = "The session has been created for the user."
                        schema = jsonSchema<CreateSessionResponse.Success>()
                    }
                    response(Unauthorized.value) {
                        description = "The provided authentication details were invalid."
                        schema = jsonSchema<CreateSessionResponse.Failed>()
                    }
                    response(UnprocessableEntity.value) {
                        description = "The body was malformed or missing data."
                    }
                }
            }
        }
    }
}
