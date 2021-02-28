package tools.caroline.admin.api

import tools.caroline.core.models.Permission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CreateProjectBody(
    val name: String,
    val description: String,
)

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
