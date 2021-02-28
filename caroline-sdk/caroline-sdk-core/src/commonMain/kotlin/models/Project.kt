package tools.caroline.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Project(
    @SerialName("_id")
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
)
