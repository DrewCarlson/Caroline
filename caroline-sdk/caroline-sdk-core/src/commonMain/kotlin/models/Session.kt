package cloud.caroline.core.models

import kotlinx.serialization.Serializable

@Serializable
public data class CreateSessionBody(
    val username: String,
    val password: String,
)

@Serializable
public sealed class CreateSessionResponse {
    @Serializable
    public data class Success(
        val user: User,
        val permissions: Set<Permission>,
    ) : CreateSessionResponse()

    @Serializable
    public data class Failed(
        val errors: Set<SessionError>,
    ) : CreateSessionResponse()

    @Serializable
    public enum class SessionError {
        USERNAME_INVALID,
        USERNAME_NOT_FOUND,
        PASSWORD_INVALID,
        PASSWORD_INCORRECT,
    }
}
