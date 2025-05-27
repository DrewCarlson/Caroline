package cloud.caroline.service

import cloud.caroline.core.models.*
import com.mongodb.MongoQueryException
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.bouncycastle.crypto.generators.OpenBSDBCrypt
import org.bson.types.ObjectId
import kotlin.random.Random

private const val USERNAME_LENGTH_MIN = 4
private const val USERNAME_LENGTH_MAX = 12
private const val PASSWORD_LENGTH_MIN = 6
private const val PASSWORD_LENGTH_MAX = 64

private const val SALT_BYTES = 16
private const val BCRYPT_COST = 10

public class CarolineUserService(
    private val users: MongoCollection<User>,
    private val credentialsDb: MongoCollection<UserCredentials>,
) {

    public suspend fun createUser(body: CreateUserBody, permissions: Set<Permission>): CreateUserResponse {
        val usernameError = when {
            body.username.isBlank() -> CreateUserResponse.UsernameError.BLANK
            body.username.length < USERNAME_LENGTH_MIN -> CreateUserResponse.UsernameError.TOO_SHORT
            body.username.length > USERNAME_LENGTH_MAX -> CreateUserResponse.UsernameError.TOO_LONG
            else -> null
        }
        val passwordError = when {
            body.password.isBlank() -> CreateUserResponse.PasswordError.BLANK
            body.password.length < PASSWORD_LENGTH_MIN -> CreateUserResponse.PasswordError.TOO_SHORT
            body.password.length > PASSWORD_LENGTH_MAX -> CreateUserResponse.PasswordError.TOO_LONG
            else -> null
        }
        val emailError = when {
            body.email.isBlank() -> CreateUserResponse.EmailError.INVALID
            else -> null
        }

        if (usernameError != null || passwordError != null || emailError != null) {
            return CreateUserResponse.Failed(usernameError, passwordError, emailError)
        }

        val username = body.username.lowercase()
        val email = body.email.lowercase()
        if (users.find(Filters.eq(User::username.name, username)).firstOrNull() != null) {
            return CreateUserResponse.Failed(CreateUserResponse.UsernameError.ALREADY_EXISTS, null, null)
        } else if (users.find(Filters.eq(User::email.name, email)).firstOrNull() != null) {
            return CreateUserResponse.Failed(null, null, CreateUserResponse.EmailError.ALREADY_EXISTS)
        }

        val user = User(
            id = ObjectId.get().toString(),
            username = username,
            displayName = body.username,
            email = email,
        )

        val credentials = UserCredentials(
            id = user.id,
            passwordHash = hashPassword(body.password),
            permissions = permissions,
        )
        return try {
            users.insertOne(user)
            credentialsDb.insertOne(credentials)
            CreateUserResponse.Success(user, credentials.permissions)
        } catch (e: MongoQueryException) {
            // TODO: Log error and better error response types
            e.printStackTrace()
            CreateUserResponse.Failed(otherError = CreateUserResponse.OtherError.SERVER_ERROR)
        }
    }

    public suspend fun createSession(body: CreateSessionBody): CreateSessionResponse {
        if (body.username.run { isBlank() || length !in USERNAME_LENGTH_MIN..USERNAME_LENGTH_MAX }) {
            return CreateSessionResponse.Failed(setOf(CreateSessionResponse.SessionError.USERNAME_INVALID))
        }

        val username = body.username.lowercase()

        if (body.password.run { isBlank() || length !in PASSWORD_LENGTH_MIN..PASSWORD_LENGTH_MAX }) {
            val errors = setOf(CreateSessionResponse.SessionError.PASSWORD_INVALID)
            return CreateSessionResponse.Failed(errors)
        }

        val user = users.find(Filters.eq(User::username.name, username))
            .firstOrNull()
            ?: return CreateSessionResponse.Failed(setOf(CreateSessionResponse.SessionError.USERNAME_NOT_FOUND))

        val auth = credentialsDb.find(Filters.eq("_id", user.id))
            .firstOrNull()
            ?: return CreateSessionResponse.Failed(setOf(CreateSessionResponse.SessionError.USERNAME_NOT_FOUND))

        return if (verifyPassword(body.password, auth.passwordHash)) {
            CreateSessionResponse.Success(user, auth.permissions)
        } else {
            CreateSessionResponse.Failed(setOf(CreateSessionResponse.SessionError.PASSWORD_INCORRECT))
        }
    }

    public suspend fun deleteUser(id: String): Boolean {
        return try {
            val result = users.deleteOne(Filters.eq("_id", id))
            credentialsDb.deleteOne(Filters.eq("_id", id))
            result.deletedCount == 1L
        } catch (e: MongoQueryException) {
            e.printStackTrace()
            false
        }
    }

    public companion object {
        public fun hashPassword(password: String): String {
            require(password.length in PASSWORD_LENGTH_MIN..PASSWORD_LENGTH_MAX) {
                "Expected password to be in $PASSWORD_LENGTH_MIN..$PASSWORD_LENGTH_MAX but was ${password.length}"
            }
            val salt = Random.nextBytes(SALT_BYTES)
            return OpenBSDBCrypt.generate(password.encodeToByteArray(), salt, BCRYPT_COST)
        }

        public fun verifyPassword(checkPassword: String, hashString: String): Boolean {
            return OpenBSDBCrypt.checkPassword(hashString, checkPassword.toCharArray())
        }
    }
}
