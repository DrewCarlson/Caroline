package cloud.caroline.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class User(
    @SerialName("_id")
    val id: String,
    val username: String,
    val displayName: String,
    val email: String?,
)

@Serializable
public data class UserCredentials(
    @SerialName("_id")
    val id: String,
    val passwordHash: String,
    val permissions: Set<Permission>,
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
    val projectId: String? = null,
    val inviteCode: String? = null,
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
        val usernameError: UsernameError? = null,
        val passwordError: PasswordError? = null,
        val emailError: EmailError? = null,
        val otherError: OtherError? = null,
    ) : CreateUserResponse()

    @Serializable
    public enum class PasswordError {
        TOO_SHORT,
        TOO_LONG,
        BLANK,
    }

    @Serializable
    public enum class UsernameError {
        TOO_SHORT,
        TOO_LONG,
        BLANK,
        ALREADY_EXISTS,
    }

    @Serializable
    public enum class EmailError {
        INVALID,
        ALREADY_EXISTS,
    }

    @Serializable
    public enum class OtherError {
        SERVER_ERROR,
    }
}
