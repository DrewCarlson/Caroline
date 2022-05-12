package cloud.caroline.models

import kotlinx.serialization.Serializable

@Serializable
public data class CreateFunctionBody(
    val name: String,
)
