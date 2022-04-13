package cloud.caroline.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class User(
    @SerialName("_id")
    val id: String,
    val username: String,
    val displayName: String,
    val email: String?
)

@Serializable
public data class UserCredentials(
    @SerialName("_id")
    val id: String,
    val passwordHash: String,
    val permissions: Set<Permission>
)

@Serializable
public data class UpdateUserBody(
    val displayName: String,
    val password: String?,
    val currentPassword: String?,
)

@Serializable
public data class CreateUserBody(
    val email: String,
    val username: String,
    val password: String,
    val inviteCode: String?,
)

@Serializable
public sealed class CreateUserResponse {

    @Serializable
    public data class Success(
        val user: User,
        val permissions: Set<Permission>,
    ) : CreateUserResponse()

    @Serializable
    public data class Failed(
        val usernameError: UsernameError?,
        val passwordError: PasswordError?,
        val emailError: EmailError?,
    ) : CreateUserResponse()

    public enum class PasswordError {
        TOO_SHORT, TOO_LONG, BLANK
    }

    public enum class UsernameError {
        TOO_SHORT, TOO_LONG, BLANK, ALREADY_EXISTS
    }

    public enum class EmailError {
        INVALID, ALREADY_EXISTS
    }
}
