package drewcarlson.caroline.data

import com.auth0.jwt.interfaces.Payload
import drewcarlson.caroline.core.models.Permission
import io.ktor.auth.Principal
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
public data class UserSession(
    val userId: String,
    override val permissions: Set<Permission>,
    val sessionStarted: Long = Instant.now().toEpochMilli(),
) : RestrictedSession {
    public companion object {
        public const val KEY: String = "session-token"
    }
}

@Serializable
public data class ProjectUserSession(
    val payload: Payload,
    val apiKey: String,
    val projectId: String,
    override val permissions: Set<Permission>
) : RestrictedSession

public interface RestrictedSession : Principal {
    public val permissions: Set<Permission>
}
