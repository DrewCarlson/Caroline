package cloud.caroline.admin.api

import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.Project
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CreateProjectBody(
    val name: String,
    val description: String,
)

@Serializable
public sealed class CreateProjectResponse {

    @Serializable
    public data class Success(
        val project: Project,
        val credentials: ApiKeyCredentials,
    ) : CreateProjectResponse()

    @Serializable
    public sealed class Failed : CreateProjectResponse() {
        public abstract val message: String

        public object InvalidRequestBody : Failed() {
            override val message: String = "The request body could not be processed."
        }
        public object ProjectNameExists : Failed() {
            override val message: String = "The project name is already in use."
        }
    }
}

@Serializable
public data class ProjectDetails(
    @SerialName("_id")
    val id: String,
    val ownerId: String,
    val apiKeys: List<String>,
    val developerIds: Set<String> = emptySet(),
)

@Serializable
public data class ApiKeyCredentials(
    @SerialName("_id")
    val apiKey: String,
    val projectId: String,
    val permissions: Set<Permission>,
)
