package cloud.caroline.data

import cloud.caroline.core.models.Permission
import com.auth0.jwt.interfaces.Payload
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
public data class UserSession(
    val userId: String,
    override val permissions: Set<Permission>,
    val sessionStarted: Long = Instant.now().toEpochMilli(),
) : RestrictedSession {
    public companion object {
        public const val KEY: String = "X-Caroline-User-Session"
    }
}

@Serializable
public data class ProjectUserSession(
    val payload: Payload?,
    val apiKey: String,
    val projectId: String,
    override val permissions: Set<Permission>,
) : RestrictedSession

public interface RestrictedSession {
    public val permissions: Set<Permission>
}
