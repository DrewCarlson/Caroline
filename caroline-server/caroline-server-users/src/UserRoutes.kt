package cloud.caroline

import com.mongodb.MongoQueryException
import cloud.caroline.core.models.CreateSessionBody
import cloud.caroline.core.models.CreateSessionResponse
import cloud.caroline.core.models.CreateSessionResponse.SessionError
import cloud.caroline.core.models.CreateUserBody
import cloud.caroline.core.models.CreateUserResponse
import cloud.caroline.core.models.CreateUserResponse.PasswordError
import cloud.caroline.core.models.CreateUserResponse.UsernameError
import cloud.caroline.core.models.CreateUserResponse.EmailError
import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.User
import cloud.caroline.core.models.UserCredentials
import cloud.caroline.data.UserSession
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.sessions.getOrSet
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import org.bouncycastle.crypto.generators.BCrypt
import org.bouncycastle.util.encoders.Hex
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import java.util.Locale
import kotlin.random.Random

private const val USERNAME_LENGTH_MIN = 4
private const val USERNAME_LENGTH_MAX = 12
private const val PASSWORD_LENGTH_MIN = 6
private const val PASSWORD_LENGTH_MAX = 64

private const val SALT_BYTES = 16
private const val BCRYPT_COST = 10

internal fun Route.addUserRoutes(mongodb: CoroutineDatabase) {

    val users = mongodb.getCollection<User>()
    val credentialsDb = mongodb.getCollection<UserCredentials>()

    route("/user") {
        post {
            val body = call.receiveOrNull<CreateUserBody>()
                ?: return@post call.respond(UnprocessableEntity)
            val createSession = call.parameters["createSession"]?.toBoolean() ?: true

            val usernameError = when {
                body.username.isBlank() -> UsernameError.BLANK
                body.username.length < USERNAME_LENGTH_MIN -> UsernameError.TOO_SHORT
                body.username.length > USERNAME_LENGTH_MAX -> UsernameError.TOO_LONG
                else -> null
            }
            val passwordError = when {
                body.password.isBlank() -> PasswordError.BLANK
                body.password.length < PASSWORD_LENGTH_MIN -> PasswordError.TOO_SHORT
                body.password.length > PASSWORD_LENGTH_MAX -> PasswordError.TOO_LONG
                else -> null
            }
            val emailError = when {
                body.email.isBlank() -> EmailError.INVALID
                else -> null
            }

            if (usernameError != null || passwordError != null || emailError != null) {
                return@post call.respond(CreateUserResponse.Failed(usernameError, passwordError, emailError))
            }

            val username = body.username.lowercase()
            val email = body.email.lowercase()
            if (users.findOne(User::username eq username) != null) {
                return@post call.respond(
                    CreateUserResponse.Failed(UsernameError.ALREADY_EXISTS, null, null)
                )
            } else if (users.findOne(User::email eq email) != null) {
                return@post call.respond(
                    CreateUserResponse.Failed(null, null, EmailError.ALREADY_EXISTS)
                )
            }

            val permissions = setOf(Permission.Global)

            val user = User(
                id = ObjectId.get().toString(),
                username = username,
                displayName = body.username,
                email = email
            )

            val salt = Random.nextBytes(SALT_BYTES)
            val passwordBytes = body.password.toByteArray()
            val hashedPassword = BCrypt.generate(passwordBytes, salt, BCRYPT_COST)

            val credentials = UserCredentials(
                id = user.id,
                password = hashedPassword.toUtf8Hex(),
                salt = salt.toUtf8Hex(),
                permissions = permissions
            )
            try {
                users.insertOne(user)
                credentialsDb.insertOne(credentials)
                if (createSession) {
                    call.sessions.getOrSet {
                        UserSession(userId = user.id, credentials.permissions)
                    }
                }

                call.respond(CreateUserResponse.Success(user, credentials.permissions))
            } catch (e: MongoQueryException) {
                call.application.log.error("Failed to insert new user", e)
                call.respond(InternalServerError)
            }
        }

        route("/session") {
            authenticate(PROVIDER_ADMIN_SESSION, optional = true) {
                post {
                    val body = call.receiveOrNull<CreateSessionBody>()
                        ?: return@post call.respond(UnprocessableEntity)

                    if (body.username.run { isBlank() || length !in USERNAME_LENGTH_MIN..USERNAME_LENGTH_MAX }) {
                        return@post call.respond(CreateSessionResponse.Failed(setOf(SessionError.USERNAME_INVALID)))
                    }

                    val username = body.username.lowercase()

                    if (body.password.run { isBlank() || length !in PASSWORD_LENGTH_MIN..PASSWORD_LENGTH_MAX }) {
                        val errors = setOf(SessionError.PASSWORD_INVALID)
                        return@post call.respond(CreateSessionResponse.Failed(errors))
                    }

                    val user = users.findOne(User::username eq username)
                        ?: return@post call.respond(CreateSessionResponse.Failed(setOf(SessionError.USERNAME_NOT_FOUND)))
                    val auth = credentialsDb.findOne(UserCredentials::id eq user.id)
                        ?: return@post call.respond(InternalServerError)

                    val saltBytes = auth.salt.utf8HexToBytes()
                    val passwordBytes = body.password.toByteArray()
                    val hashedPassword =
                        BCrypt.generate(passwordBytes, saltBytes, BCRYPT_COST).toUtf8Hex()

                    if (hashedPassword == auth.password) {
                        call.sessions.set(UserSession(user.id, auth.permissions))
                        call.respond(CreateSessionResponse.Success(user, auth.permissions))
                    } else {
                        call.respond(CreateSessionResponse.Failed(setOf(SessionError.PASSWORD_INCORRECT)))
                    }
                }
            }
        }
    }
}

private fun String.utf8HexToBytes(): ByteArray =
    toByteArray().run(Hex::decode)

private fun ByteArray.toUtf8Hex(): String =
    run(Hex::encode).toString(Charsets.UTF_8)
