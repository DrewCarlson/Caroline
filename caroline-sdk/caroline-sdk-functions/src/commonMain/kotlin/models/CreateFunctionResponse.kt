package cloud.caroline.models

import kotlinx.serialization.Serializable

@Serializable
public sealed class CreateFunctionResponse {

    @Serializable
    public data class Success(
        val name: String,
    ) : CreateFunctionResponse()

    @Serializable
    public object Failed : CreateFunctionResponse()
}
